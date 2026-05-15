package com.example.nexusa.ConfidenceScoring.service;

import com.example.nexusa.Dto.GlobalDTOs.ConfidenceScoreRequest;
import com.example.nexusa.Dto.GlobalDTOs.ConfidenceScoreResponseDTO;
import com.example.nexusa.Dto.GlobalDTOs.RecalculateScoresRequest;
import com.example.nexusa.Dto.GlobalDTOs.ScoringWeightsDTO;
import com.example.nexusa.ConfidenceScoring.model.ClaimConfidenceScore;
import com.example.nexusa.Model.Enums.GlobalEnums.ScoringConfigStatus;
import com.example.nexusa.ConfidenceScoring.model.ScoringWeightConfig;
import com.example.nexusa.Repository.GlobalRepositories.ClaimConfidenceScoreRepository;
import com.example.nexusa.Repository.GlobalRepositories.ScoringWeightConfigRepository;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictGroupStatus;
import com.example.nexusa.Repository.GlobalRepositories.ConflictGroupMemberRepository;
import com.example.nexusa.Model.ClaimEvidence;
import com.example.nexusa.Model.CitationSource;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.EvidenceType;
import com.example.nexusa.Model.Enums.GlobalEnums.Role;
import com.example.nexusa.Model.HistoricalClaim;
import com.example.nexusa.Model.User;
import com.example.nexusa.Repository.GlobalRepositories.ClaimEvidenceRepository;
import com.example.nexusa.Repository.GlobalRepositories.HistoricalClaimRepository;
import com.example.nexusa.Model.Enums.GlobalEnums.SemanticFindingStatus;
import com.example.nexusa.SemanticValidation.model.SemanticValidationResult;
import com.example.nexusa.Repository.GlobalRepositories.SemanticValidationResultRepository;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import com.example.nexusa.Model.GlobalModels.ConfidenceFactorScore;
import com.example.nexusa.Model.GlobalModels.Citation;

@Service("claimConfidenceScoringService")
@Transactional
public class ConfidenceScoringService {
    private final HistoricalClaimRepository claimRepository;
    private final ClaimEvidenceRepository evidenceRepository;
    private final ClaimConfidenceScoreRepository scoreRepository;
    private final ScoringWeightConfigRepository configRepository;
    private final SemanticValidationResultRepository semanticResultRepository;
    private final ConflictGroupMemberRepository conflictMemberRepository;
    private final ObjectMapper objectMapper;

    public ConfidenceScoringService(HistoricalClaimRepository claimRepository,
                                    ClaimEvidenceRepository evidenceRepository,
                                    ClaimConfidenceScoreRepository scoreRepository,
                                    ScoringWeightConfigRepository configRepository,
                                    SemanticValidationResultRepository semanticResultRepository,
                                    ConflictGroupMemberRepository conflictMemberRepository,
                                    ObjectMapper objectMapper) {
        this.claimRepository = claimRepository;
        this.evidenceRepository = evidenceRepository;
        this.scoreRepository = scoreRepository;
        this.configRepository = configRepository;
        this.semanticResultRepository = semanticResultRepository;
        this.conflictMemberRepository = conflictMemberRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ConfidenceScoreResponseDTO scoreClaim(ConfidenceScoreRequest request) {
        HistoricalClaim claim = claimRepository.findById(request.getClaimId())
                .orElseThrow(() -> new RuntimeException("Historical claim not found: " + request.getClaimId()));
        ScoringWeightConfig config = resolveConfig(request.getScoringVersion());
        Map<String, Double> weights = parseWeights(config.getWeights());
        List<ClaimEvidence> evidence = evidenceRepository.findByClaim_ClaimId(claim.getClaimId(), PageRequest.of(0, 1000)).getContent();

        List<ConfidenceFactorScore> factors = List.of(
                institutionReputation(claim),
                citationQuality(evidence),
                researcherReputation(claim),
                consensusSimilarity(claim),
                sourceReliability(evidence),
                moderatorApproval(claim),
                historicalConsistency(claim),
                evidenceQuality(evidence)
        );

        Map<String, Double> factorScores = factors.stream()
                .collect(Collectors.toMap(ConfidenceFactorScore::factor, ConfidenceFactorScore::score,
                        (left, right) -> left, LinkedHashMap::new));
        List<String> explanation = factors.stream()
                .map(factor -> factor.factor() + "=" + round(factor.score()) + ": " + factor.explanation())
                .toList();

        double score = weightedAverage(factorScores, weights);
        ClaimConfidenceScore persisted = null;
        if (request.isPersistScore()) {
            supersedePreviousScores(claim.getClaimId());
            ClaimConfidenceScore record = new ClaimConfidenceScore();
            record.setClaimId(claim.getClaimId());
            record.setScore(score);
            record.setConfidenceLabel(label(score));
            record.setScoringVersion(config.getVersion());
            record.setFactorScores(toJson(factorScores));
            record.setExplanation(toJson(explanation));
            record.setCalculatedBy(request.getCalculatedBy());
            persisted = scoreRepository.save(record);
        }

        if (request.isUpdateClaimConfidence()) {
            claim.setConfidenceScore(score);
            claimRepository.save(claim);
        }

        return toResponse(persisted, claim.getClaimId(), score, label(score), config.getVersion(), factorScores, explanation);
    }

    @Transactional
    public List<ConfidenceScoreResponseDTO> recalculate(RecalculateScoresRequest request) {
        List<UUID> claimIds = request.getClaimIds();
        if (claimIds == null || claimIds.isEmpty()) {
            claimIds = claimRepository.findAll().stream().map(HistoricalClaim::getClaimId).toList();
        }

        List<ConfidenceScoreResponseDTO> responses = new ArrayList<>();
        for (UUID claimId : claimIds) {
            ConfidenceScoreRequest scoreRequest = new ConfidenceScoreRequest();
            scoreRequest.setClaimId(claimId);
            scoreRequest.setScoringVersion(request.getScoringVersion());
            scoreRequest.setCalculatedBy(request.getCalculatedBy());
            scoreRequest.setPersistScore(true);
            scoreRequest.setUpdateClaimConfidence(request.isUpdateClaimConfidence());
            responses.add(scoreClaim(scoreRequest));
        }
        return responses;
    }

    public ConfidenceScoreResponseDTO getLatestScore(UUID claimId) {
        ClaimConfidenceScore score = scoreRepository.findFirstByClaimIdAndSupersededFalseOrderByCalculatedAtDesc(claimId)
                .orElseThrow(() -> new RuntimeException("No confidence score found for claim: " + claimId));
        return toResponse(score);
    }

    public List<ConfidenceScoreResponseDTO> getScoreHistory(UUID claimId) {
        return scoreRepository.findByClaimIdOrderByCalculatedAtDesc(claimId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ScoringWeightConfig createWeightConfig(ScoringWeightsDTO dto) {
        ScoringWeightConfig config = new ScoringWeightConfig();
        config.setVersion(dto.getVersion());
        config.setName(dto.getName());
        config.setWeights(toJson(dto.getWeights()));
        config.setCreatedBy(dto.getCreatedBy());
        return configRepository.save(config);
    }

    public ScoringWeightConfig getActiveConfig() {
        return resolveConfig(null);
    }

    private ConfidenceFactorScore institutionReputation(HistoricalClaim claim) {
        User user = claim.getSubmittedBy();
        if (user == null || user.getUniID() == null) {
            return factor("institutionReputation", 0.45, "No submitting institution is available.");
        }
        if (user.getUniID().getRorId() != null && !user.getUniID().getRorId().isBlank()) {
            return factor("institutionReputation", 0.90, "Submitting institution has a ROR identifier.");
        }
        return factor("institutionReputation", 0.65, "Submitting institution is known but lacks an external reputation identifier.");
    }

    private ConfidenceFactorScore citationQuality(List<ClaimEvidence> evidence) {
        if (evidence.isEmpty()) {
            return factor("citationQuality", 0.20, "No citations are attached.");
        }
        double average = evidence.stream()
                .map(ClaimEvidence::getCitationSource)
                .filter(Objects::nonNull)
                .mapToDouble(source -> evidenceTypeScore(source.getEvidenceType()))
                .average()
                .orElse(0.35);
        return factor("citationQuality", average, "Citation quality is based on evidence type mix across " + evidence.size() + " evidence records.");
    }

    private ConfidenceFactorScore researcherReputation(HistoricalClaim claim) {
        User user = claim.getSubmittedBy();
        if (user == null || user.getRole() == null) {
            return factor("researcherReputation", 0.45, "No submitter role is available.");
        }
        double score = switch (user.getRole()) {
            case ADMIN -> 0.92;
            case EDITOR -> 0.82;
            case VIEWER -> 0.55;
        };
        return factor("researcherReputation", score, "Submitter role is " + user.getRole() + ".");
    }

    private ConfidenceFactorScore consensusSimilarity(HistoricalClaim claim) {
        List<HistoricalClaim> peers = claimRepository.findBySubjectEntityIdAndPredicate(claim.getSubjectEntityId(), claim.getPredicate());
        if (peers.size() <= 1) {
            return factor("consensusSimilarity", 0.55, "No peer claims exist for consensus comparison.");
        }
        String normalized = normalizeValue(claim);
        long matching = peers.stream().filter(peer -> normalizeValue(peer).equals(normalized)).count();
        double score = (double) matching / peers.size();
        return factor("consensusSimilarity", score, matching + " of " + peers.size() + " peer claims share the same normalized value.");
    }

    private ConfidenceFactorScore sourceReliability(List<ClaimEvidence> evidence) {
        if (evidence.isEmpty()) {
            return factor("sourceReliability", 0.25, "No source reliability data is available.");
        }
        double average = evidence.stream()
                .mapToDouble(this::evidenceReliability)
                .average()
                .orElse(0.50);
        return factor("sourceReliability", average, "Average reliability was calculated from evidence overrides and citation source scores.");
    }

    private ConfidenceFactorScore moderatorApproval(HistoricalClaim claim) {
        ClaimStatus status = claim.getClaimStatus();
        double score = switch (status) {
            case CANONICAL -> 1.0;
            case VALIDATED -> 0.88;
            case PENDING -> 0.50;
            case DISPUTED -> 0.30;
            case REJECTED -> 0.05;
            case SUPERSEDED -> 0.20;
        };
        return factor("moderatorApproval", score, "Claim moderation status is " + status + ".");
    }

    private ConfidenceFactorScore historicalConsistency(HistoricalClaim claim) {
        double score = 0.85;
        List<String> reasons = new ArrayList<>();

        List<SemanticValidationResult> semanticFindings = semanticResultRepository
                .findByClaimIdOrderByCreatedAtDesc(claim.getClaimId().toString())
                .stream()
                .filter(result -> result.getStatus() == SemanticFindingStatus.OPEN)
                .toList();
        for (SemanticValidationResult finding : semanticFindings) {
            score -= switch (finding.getSeverity()) {
                case CRITICAL -> 0.35;
                case ERROR -> 0.22;
                case WARNING -> 0.10;
                case INFO -> 0.03;
            };
        }
        if (!semanticFindings.isEmpty()) {
            reasons.add(semanticFindings.size() + " open semantic validation findings.");
        }

        long openConflicts = conflictMemberRepository.findByClaimId(claim.getClaimId()).stream()
                .filter(member -> member.getConflictGroup().getStatus() == ConflictGroupStatus.OPEN ||
                        member.getConflictGroup().getStatus() == ConflictGroupStatus.UNDER_REVIEW)
                .count();
        score -= Math.min(0.35, openConflicts * 0.15);
        if (openConflicts > 0) {
            reasons.add(openConflicts + " open conflict groups include this claim.");
        }

        if (reasons.isEmpty()) {
            reasons.add("No open semantic findings or conflict groups were found.");
        }
        return factor("historicalConsistency", clamp(score), String.join(" ", reasons));
    }

    private ConfidenceFactorScore evidenceQuality(List<ClaimEvidence> evidence) {
        if (evidence.isEmpty()) {
            return factor("evidenceQuality", 0.15, "No evidence records are attached.");
        }
        double total = 0.0;
        for (ClaimEvidence item : evidence) {
            double score = 0.35;
            if (item.getEvidenceText() != null && item.getEvidenceText().length() >= 80) {
                score += 0.20;
            }
            if (item.getQuotedPassage() != null && !item.getQuotedPassage().isBlank()) {
                score += 0.20;
            }
            if (item.getPageNumbers() != null && !item.getPageNumbers().isBlank()) {
                score += 0.15;
            }
            if (item.getUploadedDocuments() != null && !item.getUploadedDocuments().isBlank()) {
                score += 0.10;
            }
            total += clamp(score);
        }
        return factor("evidenceQuality", total / evidence.size(), "Evidence quality reflects text depth, quotations, pages, and uploaded documents.");
    }

    private ScoringWeightConfig resolveConfig(Integer version) {
        if (version != null) {
            return configRepository.findByVersion(version)
                    .orElseThrow(() -> new RuntimeException("Scoring config version not found: " + version));
        }
        return configRepository.findFirstByStatusOrderByVersionDesc(ScoringConfigStatus.ACTIVE)
                .orElseGet(this::defaultConfig);
    }

    private ScoringWeightConfig defaultConfig() {
        ScoringWeightConfig config = new ScoringWeightConfig();
        config.setVersion(1);
        config.setName("Default NexusA Confidence Model");
        config.setWeights(toJson(ConfidenceScoringModel.defaultWeights()));
        config.setStatus(ScoringConfigStatus.ACTIVE);
        return configRepository.save(config);
    }

    private double evidenceTypeScore(EvidenceType type) {
        if (type == null) {
            return 0.40;
        }
        return switch (type) {
            case PRIMARY_SOURCE, ARCHAEOLOGICAL_PAPER, MANUSCRIPT, MUSEUM_ARCHIVE, GOVERNMENT_DOCUMENT -> 0.90;
            case JOURNAL_ARTICLE, DOI_REFERENCE -> 0.82;
            case BOOK, THESIS -> 0.70;
            case URL -> 0.45;
        };
    }

    private double evidenceReliability(ClaimEvidence evidence) {
        if (evidence.getSourceReliabilityScore() != null) {
            return clamp(evidence.getSourceReliabilityScore());
        }
        CitationSource source = evidence.getCitationSource();
        if (source != null && source.getReliabilityScore() != null) {
            return clamp(source.getReliabilityScore());
        }
        return 0.50;
    }

    private String normalizeValue(HistoricalClaim claim) {
        if (claim.getNormalizedValue() != null && !claim.getNormalizedValue().isBlank()) {
            return claim.getNormalizedValue().trim().toLowerCase(Locale.ROOT);
        }
        return claim.getObjectValue() == null ? "" : claim.getObjectValue().trim().toLowerCase(Locale.ROOT);
    }

    private double weightedAverage(Map<String, Double> factorScores, Map<String, Double> weights) {
        double weighted = 0.0;
        double weightTotal = 0.0;
        for (Map.Entry<String, Double> entry : weights.entrySet()) {
            Double factorScore = factorScores.get(entry.getKey());
            if (factorScore == null) {
                continue;
            }
            weighted += factorScore * entry.getValue();
            weightTotal += entry.getValue();
        }
        return round(weightTotal == 0.0 ? 0.0 : weighted / weightTotal);
    }

    private void supersedePreviousScores(UUID claimId) {
        scoreRepository.findFirstByClaimIdAndSupersededFalseOrderByCalculatedAtDesc(claimId)
                .ifPresent(previous -> {
                    previous.setSuperseded(true);
                    scoreRepository.save(previous);
                });
    }

    private String label(double score) {
        if (score >= 0.85) {
            return "HIGH";
        }
        if (score >= 0.65) {
            return "MEDIUM";
        }
        if (score >= 0.40) {
            return "LOW";
        }
        return "VERY_LOW";
    }

    private ConfidenceFactorScore factor(String name, double score, String explanation) {
        return new ConfidenceFactorScore(name, round(score), explanation);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private double round(double value) {
        return Math.round(clamp(value) * 1000.0) / 1000.0;
    }

    private Map<String, Double> parseWeights(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return ConfidenceScoringModel.defaultWeights();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<LinkedHashMap<String, Double>>() {});
        } catch (JsonProcessingException e) {
            return ConfidenceScoringModel.defaultWeights();
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private ConfidenceScoreResponseDTO toResponse(ClaimConfidenceScore score) {
        return ConfidenceScoreResponseDTO.builder()
                .scoreId(score.getScoreId())
                .claimId(score.getClaimId())
                .score(score.getScore())
                .confidenceLabel(score.getConfidenceLabel())
                .scoringVersion(score.getScoringVersion())
                .factorScores(parseWeights(score.getFactorScores()))
                .explanation(parseExplanation(score.getExplanation()))
                .calculatedAt(score.getCalculatedAt())
                .build();
    }

    private ConfidenceScoreResponseDTO toResponse(ClaimConfidenceScore persisted,
                                                  UUID claimId,
                                                  double score,
                                                  String label,
                                                  Integer version,
                                                  Map<String, Double> factorScores,
                                                  List<String> explanation) {
        return ConfidenceScoreResponseDTO.builder()
                .scoreId(persisted == null ? null : persisted.getScoreId())
                .claimId(claimId)
                .score(score)
                .confidenceLabel(label)
                .scoringVersion(version)
                .factorScores(factorScores)
                .explanation(explanation)
                .calculatedAt(persisted == null ? null : persisted.getCalculatedAt())
                .build();
    }

    private List<String> parseExplanation(String rawJson) {
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
