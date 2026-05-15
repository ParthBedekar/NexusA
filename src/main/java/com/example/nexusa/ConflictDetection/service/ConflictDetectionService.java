package com.example.nexusa.ConflictDetection.service;

import com.example.nexusa.Dto.GlobalDTOs.ConflictDetectionRequest;
import com.example.nexusa.Dto.GlobalDTOs.ConflictDetectionResponse;
import com.example.nexusa.Dto.GlobalDTOs.ConflictGroupResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.ConflictModerationRequest;
import com.example.nexusa.ConflictDetection.model.ConflictGroup;
import com.example.nexusa.ConflictDetection.model.ConflictGroupMember;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictGroupStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictType;
import com.example.nexusa.Repository.GlobalRepositories.ConflictGroupMemberRepository;
import com.example.nexusa.Repository.GlobalRepositories.ConflictGroupRepository;
import com.example.nexusa.Model.HistoricalClaim;
import com.example.nexusa.Repository.GlobalRepositories.HistoricalClaimRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.nexusa.Model.GlobalModels.ConflictSeverityScore;
import com.example.nexusa.Model.GlobalModels.NormalizedClaimValue;

@Service
@Transactional
public class ConflictDetectionService {
    private final HistoricalClaimRepository claimRepository;
    private final ConflictGroupRepository conflictGroupRepository;
    private final ConflictGroupMemberRepository memberRepository;
    private final ClaimValueNormalizer valueNormalizer;
    private final ConflictSeverityScorer severityScorer;
    private final ObjectMapper objectMapper;

    public ConflictDetectionService(HistoricalClaimRepository claimRepository,
                                    ConflictGroupRepository conflictGroupRepository,
                                    ConflictGroupMemberRepository memberRepository,
                                    ClaimValueNormalizer valueNormalizer,
                                    ConflictSeverityScorer severityScorer,
                                    ObjectMapper objectMapper) {
        this.claimRepository = claimRepository;
        this.conflictGroupRepository = conflictGroupRepository;
        this.memberRepository = memberRepository;
        this.valueNormalizer = valueNormalizer;
        this.severityScorer = severityScorer;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ConflictDetectionResponse detectConflicts(ConflictDetectionRequest request) {
        List<HistoricalClaim> claims = loadClaims(request);
        Map<String, List<HistoricalClaim>> groupedClaims = claims.stream()
                .collect(Collectors.groupingBy(claim -> claim.getSubjectEntityId() + "|" + claim.getPredicate()));

        List<ConflictGroupResponseDTO> groups = new ArrayList<>();
        int persisted = 0;
        for (List<HistoricalClaim> claimGroup : groupedClaims.values()) {
            Optional<ConflictGroupResponseDTO> group = evaluateClaimGroup(claimGroup, request);
            if (group.isPresent()) {
                groups.add(group.get());
                if (request.isPersistGroups()) {
                    persisted++;
                }
            }
        }

        groups.sort(Comparator.comparing(ConflictGroupResponseDTO::getSeverityScore).reversed());
        return ConflictDetectionResponse.builder()
                .totalGroups(groups.size())
                .persistedGroups(persisted)
                .groups(groups)
                .build();
    }

    @Transactional
    public ConflictDetectionResponse detectConflictsForClaim(UUID claimId, boolean persistGroups) {
        HistoricalClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Historical claim not found: " + claimId));
        ConflictDetectionRequest request = new ConflictDetectionRequest();
        request.setSubjectEntityId(claim.getSubjectEntityId());
        request.setPredicate(claim.getPredicate());
        request.setPersistGroups(persistGroups);
        return detectConflicts(request);
    }

    public List<ConflictGroupResponseDTO> getModerationQueue(ConflictGroupStatus status) {
        ConflictGroupStatus queueStatus = status == null ? ConflictGroupStatus.OPEN : status;
        return conflictGroupRepository.findByStatusOrderBySeverityScoreDesc(queueStatus).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ConflictGroupResponseDTO> getConflictsForSubject(UUID subjectEntityId) {
        return conflictGroupRepository.findBySubjectEntityIdOrderBySeverityScoreDesc(subjectEntityId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConflictGroupResponseDTO moderateConflict(UUID conflictGroupId, ConflictModerationRequest request) {
        ConflictGroup group = conflictGroupRepository.findById(conflictGroupId)
                .orElseThrow(() -> new RuntimeException("Conflict group not found: " + conflictGroupId));
        group.setStatus(request.getStatus() == null ? group.getStatus() : request.getStatus());
        group.setReviewedBy(request.getReviewedBy());
        group.setReviewNotes(request.getReviewNotes());
        group.setResolvedClaimId(request.getResolvedClaimId());
        if (group.getStatus() == ConflictGroupStatus.RESOLVED || group.getStatus() == ConflictGroupStatus.DISMISSED) {
            group.setResolvedAt(LocalDateTime.now());
        }
        return toResponse(conflictGroupRepository.save(group));
    }

    private List<HistoricalClaim> loadClaims(ConflictDetectionRequest request) {
        if (request.getSubjectEntityId() != null && request.getPredicate() != null && !request.getPredicate().isBlank()) {
            return claimRepository.findBySubjectEntityIdAndPredicate(request.getSubjectEntityId(), request.getPredicate());
        }
        if (request.getSubjectEntityId() != null) {
            return claimRepository.findBySubjectEntityId(request.getSubjectEntityId(), org.springframework.data.domain.Pageable.unpaged()).getContent();
        }
        return claimRepository.findAll();
    }

    private Optional<ConflictGroupResponseDTO> evaluateClaimGroup(List<HistoricalClaim> claims, ConflictDetectionRequest request) {
        if (claims.size() < 2) {
            return Optional.empty();
        }

        Map<String, List<HistoricalClaim>> claimsByValue = new LinkedHashMap<>();
        Map<String, NormalizedClaimValue> normalizedValues = new LinkedHashMap<>();
        for (HistoricalClaim claim : claims) {
            NormalizedClaimValue value = valueNormalizer.normalize(claim);
            if (value.normalizedValue().isBlank()) {
                continue;
            }
            claimsByValue.computeIfAbsent(value.normalizedValue(), ignored -> new ArrayList<>()).add(claim);
            normalizedValues.putIfAbsent(value.normalizedValue(), value);
        }

        if (claimsByValue.size() < 2) {
            return Optional.empty();
        }

        ConflictType type = normalizedValues.values().stream()
                .findFirst()
                .map(NormalizedClaimValue::conflictType)
                .orElse(ConflictType.FACT);
        ConflictSeverityScore score = severityScorer.score(type, normalizedValues.values());
        if (score.severityScore() < request.getMinimumSeverityScore()) {
            return Optional.empty();
        }

        HistoricalClaim representative = claims.get(0);
        List<String> rawValues = normalizedValues.values().stream().map(NormalizedClaimValue::rawValue).distinct().toList();
        List<UUID> claimIds = claims.stream().map(HistoricalClaim::getClaimId).toList();
        ConflictGroup group = buildGroup(representative, type, score, rawValues, claimIds);

        if (request.isPersistGroups()) {
            group = persistGroup(group, claims, normalizedValues);
            return Optional.of(toResponse(group));
        }
        return Optional.of(toResponse(group, claimIds, rawValues));
    }

    private ConflictGroup buildGroup(HistoricalClaim representative,
                                     ConflictType type,
                                     ConflictSeverityScore score,
                                     List<String> rawValues,
                                     List<UUID> claimIds) {
        ConflictGroup group = new ConflictGroup();
        group.setSubjectEntityId(representative.getSubjectEntityId());
        group.setSubjectEntityType(representative.getSubjectEntityType());
        group.setPredicate(representative.getPredicate());
        group.setConflictType(type);
        group.setSeverity(score.severity());
        group.setSeverityScore(score.severityScore());
        group.setConfidenceScore(score.confidenceScore());
        group.setSummary(score.explanation());
        group.setConflictingValues(toJson(rawValues));
        group.setEvidence(toJson(Map.of("claimIds", claimIds, "distinctValueCount", rawValues.size())));
        return group;
    }

    private ConflictGroup persistGroup(ConflictGroup group,
                                       List<HistoricalClaim> claims,
                                       Map<String, NormalizedClaimValue> normalizedValues) {
        ConflictGroup persisted = conflictGroupRepository
                .findFirstBySubjectEntityIdAndPredicateAndConflictTypeAndStatusIn(
                        group.getSubjectEntityId(),
                        group.getPredicate(),
                        group.getConflictType(),
                        List.of(ConflictGroupStatus.OPEN, ConflictGroupStatus.UNDER_REVIEW))
                .orElse(group);

        persisted.setSubjectEntityType(group.getSubjectEntityType());
        persisted.setSeverity(group.getSeverity());
        persisted.setSeverityScore(group.getSeverityScore());
        persisted.setConfidenceScore(group.getConfidenceScore());
        persisted.setSummary(group.getSummary());
        persisted.setConflictingValues(group.getConflictingValues());
        persisted.setEvidence(group.getEvidence());
        persisted = conflictGroupRepository.save(persisted);

        Map<UUID, NormalizedClaimValue> valueByClaimId = new HashMap<>();
        for (HistoricalClaim claim : claims) {
            NormalizedClaimValue value = valueNormalizer.normalize(claim);
            valueByClaimId.put(claim.getClaimId(), value);
        }

        for (HistoricalClaim claim : claims) {
            if (!memberRepository.existsByConflictGroup_ConflictGroupIdAndClaimId(persisted.getConflictGroupId(), claim.getClaimId())) {
                ConflictGroupMember member = new ConflictGroupMember();
                member.setConflictGroup(persisted);
                member.setClaimId(claim.getClaimId());
                member.setNormalizedValue(valueByClaimId.get(claim.getClaimId()).normalizedValue());
                member.setClaimConfidence(claim.getConfidenceScore());
                memberRepository.save(member);
            }
        }
        return persisted;
    }

    private ConflictGroupResponseDTO toResponse(ConflictGroup group) {
        List<ConflictGroupMember> members = memberRepository.findByConflictGroup_ConflictGroupId(group.getConflictGroupId());
        List<UUID> claimIds = members.stream().map(ConflictGroupMember::getClaimId).toList();
        return toResponse(group, claimIds, parseStringList(group.getConflictingValues()));
    }

    private ConflictGroupResponseDTO toResponse(ConflictGroup group, List<UUID> claimIds, List<String> values) {
        return ConflictGroupResponseDTO.builder()
                .conflictGroupId(group.getConflictGroupId())
                .subjectEntityId(group.getSubjectEntityId())
                .subjectEntityType(group.getSubjectEntityType())
                .predicate(group.getPredicate())
                .conflictType(group.getConflictType())
                .severity(group.getSeverity())
                .severityScore(group.getSeverityScore())
                .confidenceScore(group.getConfidenceScore())
                .summary(group.getSummary())
                .conflictingValues(values)
                .claimIds(claimIds)
                .status(group.getStatus())
                .resolvedClaimId(group.getResolvedClaimId())
                .createdAt(group.getCreatedAt())
                .build();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> parseStringList(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
