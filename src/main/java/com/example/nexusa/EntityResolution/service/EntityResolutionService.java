package com.example.nexusa.EntityResolution.service;
import com.example.nexusa.Model.Enums.GlobalEnums.DuplicateCandidateStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.CanonicalEntityStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.AliasType;

import com.example.nexusa.Dto.GlobalDTOs.*;
import com.example.nexusa.EntityResolution.model.*;
import com.example.nexusa.Repository.GlobalRepositories.ResolutionCanonicalEntityRepository;
import com.example.nexusa.Repository.GlobalRepositories.DuplicateCandidateRepository;
import com.example.nexusa.Repository.GlobalRepositories.EntityAliasRepository;
import com.example.nexusa.Repository.GlobalRepositories.EntityMergeAuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import com.example.nexusa.Model.GlobalModels.SimilarityScore;

@Service
@Transactional
public class EntityResolutionService {
    private static final int SEARCH_LIMIT = 25;

    private final ResolutionCanonicalEntityRepository canonicalRepository;
    private final EntityAliasRepository aliasRepository;
    private final DuplicateCandidateRepository duplicateRepository;
    private final EntityMergeAuditRepository mergeAuditRepository;
    private final EntityNameNormalizer normalizer;
    private final FuzzyEntityMatcher matcher;
    private final ObjectMapper objectMapper;

    public EntityResolutionService(ResolutionCanonicalEntityRepository canonicalRepository,
                                   EntityAliasRepository aliasRepository,
                                   DuplicateCandidateRepository duplicateRepository,
                                   EntityMergeAuditRepository mergeAuditRepository,
                                   EntityNameNormalizer normalizer,
                                   FuzzyEntityMatcher matcher,
                                   ObjectMapper objectMapper) {
        this.canonicalRepository = canonicalRepository;
        this.aliasRepository = aliasRepository;
        this.duplicateRepository = duplicateRepository;
        this.mergeAuditRepository = mergeAuditRepository;
        this.normalizer = normalizer;
        this.matcher = matcher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CanonicalEntityResponseDTO createCanonicalEntity(CanonicalEntityCreateDTO dto) {
        String normalizedName = normalizer.normalize(dto.getCanonicalName());
        canonicalRepository.findByEntityTypeIgnoreCaseAndNormalizedNameAndStatusNot(
                dto.getEntityType(), normalizedName, CanonicalEntityStatus.MERGED
        ).ifPresent(existing -> {
            throw new RuntimeException("Canonical entity already exists: " + existing.getCanonicalId());
        });

        CanonicalEntity entity = new CanonicalEntity();
        entity.setEntityType(dto.getEntityType());
        entity.setCanonicalName(dto.getCanonicalName());
        entity.setNormalizedName(normalizedName);
        entity.setDescription(dto.getDescription());
        entity.setStartYear(dto.getStartYear());
        entity.setEndYear(dto.getEndYear());
        entity.setSourceEntityId(dto.getSourceEntityId());
        entity.setConfidenceScore(dto.getConfidenceScore() == null ? 1.0 : dto.getConfidenceScore());
        entity = canonicalRepository.save(entity);

        createAlias(entity, dto.getCanonicalName(), AliasType.PRIMARY, null, "canonical-name", 1.0, null);
        if (dto.getAliases() != null) {
            for (String alias : dto.getAliases()) {
                createAlias(entity, alias, AliasType.ALIAS, null, "create-request", 0.90, null);
            }
        }

        return toResponse(entity);
    }

    @Transactional
    public CanonicalEntityResponseDTO addAlias(UUID canonicalId, EntityAliasCreateDTO dto) {
        CanonicalEntity entity = activeEntity(canonicalId);
        createAlias(entity, dto.getAliasName(), dto.getAliasType(), dto.getLanguageCode(),
                dto.getSource(), dto.getConfidenceScore(), null);
        return toResponse(entity);
    }

    public List<EntityMatchDTO> resolve(EntityResolutionRequest request) {
        String normalized = normalizer.normalize(request.getName());
        List<EntityMatchDTO> matches = new ArrayList<>();

        aliasRepository.findFirstByNormalizedAliasAndCanonicalEntity_EntityTypeIgnoreCase(normalized, request.getEntityType())
                .ifPresent(alias -> matches.add(match(alias.getCanonicalEntity(), 1.0, "EXACT_ALIAS",
                        true, List.of("exactAlias=" + alias.getAliasName()))));

        canonicalRepository.findByEntityTypeIgnoreCaseAndNormalizedNameAndStatusNot(
                request.getEntityType(), normalized, CanonicalEntityStatus.MERGED
        ).ifPresent(entity -> matches.add(match(entity, 1.0, "EXACT_CANONICAL_NAME", false, List.of("exactCanonicalName=true"))));

        Set<UUID> seen = new HashSet<>();
        List<EntityMatchDTO> ranked = new ArrayList<>();
        for (EntityMatchDTO exact : matches) {
            if (seen.add(exact.getCanonicalId())) {
                ranked.add(exact);
            }
        }

        for (CanonicalEntity candidate : searchCandidates(request.getEntityType(), normalized)) {
            if (!seen.add(candidate.getCanonicalId())) {
                continue;
            }
            SimilarityScore score = matcher.score(request.getName(), request.getEntityType(),
                    request.getStartYear(), request.getEndYear(), candidate, aliases(candidate.getCanonicalId()));
            if (score.totalScore() >= request.getThreshold()) {
                ranked.add(match(candidate, score.totalScore(), "FUZZY_CANONICAL", score.aliasMatch(), score.evidence()));
            }
        }

        for (EntityAlias alias : searchAliases(request.getEntityType(), normalized)) {
            CanonicalEntity candidate = alias.getCanonicalEntity();
            if (!seen.add(candidate.getCanonicalId())) {
                continue;
            }
            SimilarityScore score = matcher.score(request.getName(), request.getEntityType(),
                    request.getStartYear(), request.getEndYear(), candidate, aliases(candidate.getCanonicalId()));
            if (score.totalScore() >= request.getThreshold()) {
                List<String> evidence = new ArrayList<>(score.evidence());
                evidence.add("matchedAlias=" + alias.getAliasName());
                ranked.add(match(candidate, Math.max(score.totalScore(), 0.80), "FUZZY_ALIAS", true, evidence));
            }
        }

        ranked.sort(Comparator.comparing(EntityMatchDTO::getSimilarityScore).reversed());
        return ranked;
    }

    @Transactional
    public List<DuplicateCandidateResponseDTO> detectDuplicates(DuplicateDetectionRequest request) {
        List<DuplicateCandidateResponseDTO> responses = new ArrayList<>();
        if (request.getCanonicalId() != null) {
            CanonicalEntity target = activeEntity(request.getCanonicalId());
            List<CanonicalEntity> candidates = canonicalRepository.findByEntityTypeIgnoreCaseAndStatusNot(
                    target.getEntityType(), CanonicalEntityStatus.MERGED);
            for (CanonicalEntity candidate : candidates) {
                if (!target.getCanonicalId().equals(candidate.getCanonicalId())) {
                    evaluateDuplicate(target, candidate, request.getThreshold(), request.isPersistCandidates()).ifPresent(responses::add);
                }
            }
            return responses;
        }

        List<CanonicalEntity> entities = request.getEntityType() == null || request.getEntityType().isBlank()
                ? canonicalRepository.findAll()
                : canonicalRepository.findByEntityTypeIgnoreCaseAndStatusNot(request.getEntityType(), CanonicalEntityStatus.MERGED);
        for (int i = 0; i < entities.size(); i++) {
            CanonicalEntity left = entities.get(i);
            if (left.getStatus() == CanonicalEntityStatus.MERGED) {
                continue;
            }
            for (int j = i + 1; j < entities.size(); j++) {
                CanonicalEntity right = entities.get(j);
                if (right.getStatus() == CanonicalEntityStatus.MERGED) {
                    continue;
                }
                evaluateDuplicate(left, right, request.getThreshold(), request.isPersistCandidates()).ifPresent(responses::add);
            }
        }
        responses.sort(Comparator.comparing(DuplicateCandidateResponseDTO::getSimilarityScore).reversed());
        return responses;
    }

    public List<DuplicateCandidateResponseDTO> getDuplicateQueue(DuplicateCandidateStatus status) {
        DuplicateCandidateStatus queueStatus = status == null ? DuplicateCandidateStatus.PENDING : status;
        return duplicateRepository.findByStatusOrderBySimilarityScoreDesc(queueStatus).stream()
                .map(this::toDuplicateResponse)
                .toList();
    }

    @Transactional
    public DuplicateCandidateResponseDTO moderateDuplicate(UUID candidateId, ModerateDuplicateRequest request) {
        DuplicateCandidate candidate = duplicateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Duplicate candidate not found: " + candidateId));
        candidate.setStatus(request.getStatus());
        candidate.setReviewedBy(request.getReviewedBy());
        candidate.setReviewNotes(request.getReviewNotes());
        candidate.setReviewedAt(LocalDateTime.now());
        return toDuplicateResponse(duplicateRepository.save(candidate));
    }

    @Transactional
    public CanonicalEntityResponseDTO mergeEntities(MergeEntitiesRequest request) {
        if (request.getSourceCanonicalId().equals(request.getTargetCanonicalId())) {
            throw new RuntimeException("Source and target canonical IDs must be different");
        }

        CanonicalEntity source = activeEntity(request.getSourceCanonicalId());
        CanonicalEntity target = activeEntity(request.getTargetCanonicalId());
        String sourceSnapshot = toJson(toSnapshot(source));
        String targetSnapshot = toJson(toSnapshot(target));

        if (request.isCopyAliases()) {
            for (EntityAlias alias : aliases(source.getCanonicalId())) {
                createAliasIfMissing(target, alias.getAliasName(), alias.getAliasType(), alias.getLanguageCode(),
                        "merge:" + source.getCanonicalId(), alias.getConfidenceScore(), request.getMergedBy());
            }
            createAliasIfMissing(target, source.getCanonicalName(), AliasType.FORMER_NAME, null,
                    "merged-canonical-name", 0.95, request.getMergedBy());
        }

        source.setStatus(CanonicalEntityStatus.MERGED);
        source.setMergedIntoId(target.getCanonicalId());
        canonicalRepository.save(source);

        EntityMergeAudit audit = new EntityMergeAudit();
        audit.setSourceCanonicalId(source.getCanonicalId());
        audit.setTargetCanonicalId(target.getCanonicalId());
        audit.setMergedBy(request.getMergedBy());
        audit.setReason(request.getReason());
        audit.setSourceSnapshot(sourceSnapshot);
        audit.setTargetSnapshot(targetSnapshot);
        mergeAuditRepository.save(audit);

        markCandidatesMerged(source.getCanonicalId(), target.getCanonicalId());
        return toResponse(target);
    }

    public CanonicalEntityResponseDTO getCanonicalEntity(UUID canonicalId) {
        return toResponse(canonicalRepository.findById(canonicalId)
                .orElseThrow(() -> new RuntimeException("Canonical entity not found: " + canonicalId)));
    }

    private Optional<DuplicateCandidateResponseDTO> evaluateDuplicate(CanonicalEntity left,
                                                                       CanonicalEntity right,
                                                                       double threshold,
                                                                       boolean persist) {
        SimilarityScore score = matcher.score(left.getCanonicalName(), left.getEntityType(),
                left.getStartYear(), left.getEndYear(), right, aliases(right.getCanonicalId()));
        if (score.totalScore() < threshold) {
            return Optional.empty();
        }

        DuplicateCandidate candidate = new DuplicateCandidate();
        CanonicalEntity orderedLeft = left.getCanonicalId().compareTo(right.getCanonicalId()) <= 0 ? left : right;
        CanonicalEntity orderedRight = orderedLeft == left ? right : left;

        Optional<DuplicateCandidate> existing = duplicateRepository.findFirstByLeftEntity_CanonicalIdAndRightEntity_CanonicalId(
                orderedLeft.getCanonicalId(), orderedRight.getCanonicalId());
        if (existing.isPresent()) {
            return Optional.of(toDuplicateResponse(existing.get()));
        }

        candidate.setLeftEntity(orderedLeft);
        candidate.setRightEntity(orderedRight);
        candidate.setSimilarityScore(score.totalScore());
        candidate.setStrategy(score.aliasMatch() ? "ALIAS_AND_FUZZY" : "FUZZY_NAME_TEMPORAL");
        candidate.setExplanation("Possible duplicate canonical entities based on fuzzy name, alias, and temporal similarity.");
        candidate.setEvidence(toJson(score.evidence()));

        if (persist) {
            candidate = duplicateRepository.save(candidate);
        }
        return Optional.of(toDuplicateResponse(candidate));
    }

    private List<CanonicalEntity> searchCandidates(String entityType, String normalizedName) {
        try {
            return canonicalRepository.trigramSearch(entityType, normalizedName, 0.20, SEARCH_LIMIT);
        } catch (RuntimeException ignored) {
            if (entityType == null || entityType.isBlank()) {
                return canonicalRepository.findAll();
            }
            return canonicalRepository.findByEntityTypeIgnoreCaseAndStatusNot(entityType, CanonicalEntityStatus.MERGED);
        }
    }

    private List<EntityAlias> searchAliases(String entityType, String normalizedAlias) {
        try {
            return aliasRepository.trigramSearch(entityType, normalizedAlias, 0.20, SEARCH_LIMIT);
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private CanonicalEntity activeEntity(UUID canonicalId) {
        CanonicalEntity entity = canonicalRepository.findById(canonicalId)
                .orElseThrow(() -> new RuntimeException("Canonical entity not found: " + canonicalId));
        if (entity.getStatus() == CanonicalEntityStatus.MERGED) {
            throw new RuntimeException("Canonical entity has already been merged into " + entity.getMergedIntoId());
        }
        return entity;
    }

    private EntityAlias createAlias(CanonicalEntity entity, String aliasName, AliasType aliasType, String languageCode,
                                    String source, Double confidenceScore, UUID createdBy) {
        EntityAlias alias = new EntityAlias();
        alias.setCanonicalEntity(entity);
        alias.setAliasName(aliasName);
        alias.setNormalizedAlias(normalizer.normalize(aliasName));
        alias.setAliasType(aliasType == null ? AliasType.ALIAS : aliasType);
        alias.setLanguageCode(languageCode);
        alias.setSource(source);
        alias.setConfidenceScore(confidenceScore == null ? 1.0 : confidenceScore);
        alias.setCreatedBy(createdBy);
        return aliasRepository.save(alias);
    }

    private void createAliasIfMissing(CanonicalEntity entity, String aliasName, AliasType aliasType, String languageCode,
                                      String source, Double confidenceScore, UUID createdBy) {
        String normalizedAlias = normalizer.normalize(aliasName);
        boolean exists = aliases(entity.getCanonicalId()).stream()
                .anyMatch(alias -> alias.getNormalizedAlias().equals(normalizedAlias));
        if (!exists) {
            createAlias(entity, aliasName, aliasType, languageCode, source, confidenceScore, createdBy);
        }
    }

    private void markCandidatesMerged(UUID sourceId, UUID targetId) {
        for (DuplicateCandidate candidate : duplicateRepository.findByLeftEntity_CanonicalIdOrRightEntity_CanonicalIdOrderBySimilarityScoreDesc(sourceId, sourceId)) {
            if (candidate.getLeftEntity().getCanonicalId().equals(targetId) || candidate.getRightEntity().getCanonicalId().equals(targetId)) {
                candidate.setStatus(DuplicateCandidateStatus.MERGED);
                candidate.setReviewedAt(LocalDateTime.now());
                duplicateRepository.save(candidate);
            }
        }
    }

    private List<EntityAlias> aliases(UUID canonicalId) {
        return aliasRepository.findByCanonicalEntity_CanonicalId(canonicalId);
    }

    private EntityMatchDTO match(CanonicalEntity entity, double score, String strategy, boolean aliasMatch, List<String> evidence) {
        return EntityMatchDTO.builder()
                .canonicalId(entity.getCanonicalId())
                .entityType(entity.getEntityType())
                .canonicalName(entity.getCanonicalName())
                .similarityScore(score)
                .strategy(strategy)
                .aliasMatch(aliasMatch)
                .evidence(evidence)
                .build();
    }

    private CanonicalEntityResponseDTO toResponse(CanonicalEntity entity) {
        return CanonicalEntityResponseDTO.builder()
                .canonicalId(entity.getCanonicalId())
                .entityType(entity.getEntityType())
                .canonicalName(entity.getCanonicalName())
                .normalizedName(entity.getNormalizedName())
                .description(entity.getDescription())
                .startYear(entity.getStartYear())
                .endYear(entity.getEndYear())
                .confidenceScore(entity.getConfidenceScore())
                .status(entity.getStatus())
                .mergedIntoId(entity.getMergedIntoId())
                .aliases(aliases(entity.getCanonicalId()).stream().map(EntityAlias::getAliasName).toList())
                .build();
    }

    private DuplicateCandidateResponseDTO toDuplicateResponse(DuplicateCandidate candidate) {
        return DuplicateCandidateResponseDTO.builder()
                .candidateId(candidate.getCandidateId())
                .leftCanonicalId(candidate.getLeftEntity().getCanonicalId())
                .rightCanonicalId(candidate.getRightEntity().getCanonicalId())
                .leftName(candidate.getLeftEntity().getCanonicalName())
                .rightName(candidate.getRightEntity().getCanonicalName())
                .similarityScore(candidate.getSimilarityScore())
                .strategy(candidate.getStrategy())
                .explanation(candidate.getExplanation())
                .status(candidate.getStatus())
                .build();
    }

    private Map<String, Object> toSnapshot(CanonicalEntity entity) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("canonicalId", entity.getCanonicalId());
        snapshot.put("entityType", entity.getEntityType());
        snapshot.put("canonicalName", entity.getCanonicalName());
        snapshot.put("normalizedName", entity.getNormalizedName());
        snapshot.put("startYear", entity.getStartYear());
        snapshot.put("endYear", entity.getEndYear());
        snapshot.put("status", entity.getStatus());
        snapshot.put("aliases", aliases(entity.getCanonicalId()).stream().map(EntityAlias::getAliasName).toList());
        return snapshot;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
