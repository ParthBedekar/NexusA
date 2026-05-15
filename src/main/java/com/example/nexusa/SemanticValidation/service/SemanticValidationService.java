package com.example.nexusa.SemanticValidation.service;

import com.example.nexusa.Model.Civilization;
import com.example.nexusa.Model.HistoricalClaim;
import com.example.nexusa.Repository.GlobalRepositories.CivilizationRepository;
import com.example.nexusa.Repository.GlobalRepositories.HistoricalClaimRepository;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimInput;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalEntityNode;
import com.example.nexusa.Dto.GlobalDTOs.SemanticGraphValidationRequest;
import com.example.nexusa.Dto.GlobalDTOs.SemanticValidationFindingDTO;
import com.example.nexusa.Dto.GlobalDTOs.SemanticValidationResponse;
import com.example.nexusa.SemanticValidation.graph.GraphValidationContext;
import com.example.nexusa.SemanticValidation.graph.HistoricalGraph;
import com.example.nexusa.Model.Enums.GlobalEnums.SemanticFindingStatus;
import com.example.nexusa.SemanticValidation.model.SemanticValidationResult;
import com.example.nexusa.Repository.GlobalRepositories.SemanticValidationResultRepository;
import com.example.nexusa.Validation.rules.GlobalRules.SemanticValidationRule;
import com.example.nexusa.SemanticValidation.temporal.TemporalReasoner;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class SemanticValidationService {
    private final List<SemanticValidationRule> rules;
    private final TemporalReasoner temporalReasoner;
    private final ConfidenceScoringService confidenceScoringService;
    private final AiAnomalyDetector aiAnomalyDetector;
    private final SemanticValidationResultRepository resultRepository;
    private final HistoricalClaimRepository claimRepository;
    private final CivilizationRepository civilizationRepository;
    private final ObjectMapper objectMapper;

    public SemanticValidationService(List<SemanticValidationRule> rules,
                                     TemporalReasoner temporalReasoner,
                                     ConfidenceScoringService confidenceScoringService,
                                     AiAnomalyDetector aiAnomalyDetector,
                                     SemanticValidationResultRepository resultRepository,
                                     HistoricalClaimRepository claimRepository,
                                     CivilizationRepository civilizationRepository,
                                     ObjectMapper objectMapper) {
        this.rules = rules;
        this.temporalReasoner = temporalReasoner;
        this.confidenceScoringService = confidenceScoringService;
        this.aiAnomalyDetector = aiAnomalyDetector;
        this.resultRepository = resultRepository;
        this.claimRepository = claimRepository;
        this.civilizationRepository = civilizationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SemanticValidationResponse validateGraph(SemanticGraphValidationRequest request) {
        HistoricalGraph graph = new HistoricalGraph(request.getNodes(), request.getEdges(), request.getClaims());
        GraphValidationContext context = new GraphValidationContext(
                graph,
                temporalReasoner,
                confidenceScoringService,
                aiAnomalyDetector,
                request.isIncludeAiChecks()
        );

        List<SemanticValidationFindingDTO> findings = new ArrayList<>();
        for (SemanticValidationRule rule : rules) {
            findings.addAll(rule.validate(context));
        }

        if (request.isPersistResults()) {
            persist(findings);
        }

        return response(findings);
    }

    @Transactional
    public SemanticValidationResponse validateClaim(UUID claimId, boolean persistResults) {
        HistoricalClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Historical claim not found: " + claimId));

        SemanticGraphValidationRequest request = new SemanticGraphValidationRequest();
        request.setPersistResults(persistResults);
        request.setIncludeAiChecks(true);
        request.getNodes().add(subjectNode(claim));
        request.getClaims().add(claimInput(claim));

        String objectEntityId = extractString(claim.getObjectValue(), "objectEntityId", "entityId", "id");
        String objectEntityType = extractString(claim.getObjectValue(), "objectEntityType", "entityType", "type");
        String objectName = extractString(claim.getObjectValue(), "name", "title", "label");
        if (objectEntityId != null) {
            HistoricalEntityNode objectNode = new HistoricalEntityNode();
            objectNode.setId(objectEntityId);
            objectNode.setType(objectEntityType == null ? "ENTITY" : objectEntityType);
            objectNode.setName(objectName == null ? objectEntityId : objectName);
            objectNode.setStartYear(extractLong(claim.getObjectValue(), "startYear", "startDate"));
            objectNode.setEndYear(extractLong(claim.getObjectValue(), "endYear", "endDate"));
            objectNode.setLatitude(extractDouble(claim.getObjectValue(), "latitude", "lat"));
            objectNode.setLongitude(extractDouble(claim.getObjectValue(), "longitude", "lng", "lon"));
            request.getNodes().add(objectNode);
        }

        return validateGraph(request);
    }

    @Transactional
    public SemanticValidationResponse validateCivilization(UUID civilizationId, boolean persistResults) {
        Civilization civilization = civilizationRepository.findById(civilizationId)
                .orElseThrow(() -> new RuntimeException("Civilization not found: " + civilizationId));

        HistoricalEntityNode node = new HistoricalEntityNode();
        node.setId(civilization.getCivId().toString());
        node.setType("CIVILIZATION");
        node.setName(civilization.getTitle());
        node.setStartYear(civilization.getStartDate());
        node.setEndYear(civilization.getEndDate());

        SemanticGraphValidationRequest request = new SemanticGraphValidationRequest();
        request.setPersistResults(persistResults);
        request.setIncludeAiChecks(false);
        request.getNodes().add(node);
        return validateGraph(request);
    }

    public List<SemanticValidationResult> getResults(String entityId, String claimId, SemanticFindingStatus status) {
        if (entityId != null && !entityId.isBlank()) {
            return resultRepository.findByEntityIdOrderByCreatedAtDesc(entityId);
        }
        if (claimId != null && !claimId.isBlank()) {
            return resultRepository.findByClaimIdOrderByCreatedAtDesc(claimId);
        }
        if (status != null) {
            return resultRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        return resultRepository.findAll();
    }

    @Transactional
    public SemanticValidationResult updateStatus(UUID resultId, SemanticFindingStatus status) {
        SemanticValidationResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("Semantic validation result not found: " + resultId));
        result.setStatus(status);
        if (status == SemanticFindingStatus.RESOLVED || status == SemanticFindingStatus.DISMISSED) {
            result.setResolvedAt(LocalDateTime.now());
        }
        return resultRepository.save(result);
    }

    private void persist(List<SemanticValidationFindingDTO> findings) {
        for (SemanticValidationFindingDTO finding : findings) {
            SemanticValidationResult result = new SemanticValidationResult();
            result.setRuleCode(finding.getRuleCode());
            result.setViolationType(finding.getViolationType());
            result.setSeverity(finding.getSeverity());
            result.setConfidence(finding.getConfidence());
            result.setEntityId(finding.getEntityId());
            result.setClaimId(finding.getClaimId());
            result.setMessage(finding.getMessage());
            result.setEvidence(toJson(finding.getEvidence()));
            result.setMetadata(toJson(finding.getMetadata()));
            resultRepository.save(result);
        }
    }

    private SemanticValidationResponse response(List<SemanticValidationFindingDTO> findings) {
        long critical = findings.stream().filter(f -> f.getSeverity() == ValidationSeverity.CRITICAL).count();
        long errors = findings.stream().filter(f -> f.getSeverity() == ValidationSeverity.ERROR).count();
        long warnings = findings.stream().filter(f -> f.getSeverity() == ValidationSeverity.WARNING).count();
        long infos = findings.stream().filter(f -> f.getSeverity() == ValidationSeverity.INFO).count();
        return SemanticValidationResponse.builder()
                .totalFindings(findings.size())
                .criticalCount((int) critical)
                .errorCount((int) errors)
                .warningCount((int) warnings)
                .infoCount((int) infos)
                .passed(critical == 0 && errors == 0)
                .findings(findings)
                .build();
    }

    private HistoricalEntityNode subjectNode(HistoricalClaim claim) {
        HistoricalEntityNode node = new HistoricalEntityNode();
        node.setId(claim.getSubjectEntityId().toString());
        node.setType(claim.getSubjectEntityType());
        node.setName(claim.getSubjectEntityType() + " " + claim.getSubjectEntityId());
        return node;
    }

    private HistoricalClaimInput claimInput(HistoricalClaim claim) {
        HistoricalClaimInput input = new HistoricalClaimInput();
        input.setId(claim.getClaimId().toString());
        input.setSubjectEntityId(claim.getSubjectEntityId().toString());
        input.setPredicate(claim.getPredicate());
        input.setObjectEntityId(extractString(claim.getObjectValue(), "objectEntityId", "entityId", "id"));
        input.setStartYear(extractLong(claim.getObjectValue(), "claimYear", "year", "startYear", "startDate"));
        input.setEndYear(extractLong(claim.getObjectValue(), "endYear", "endDate"));
        input.setConfidence(claim.getConfidenceScore());
        input.setSourceReliability(extractDouble(claim.getObjectValue(), "sourceReliability", "reliability"));
        input.setClaimText(claim.getNormalizedValue() == null ? claim.getPredicate() + " " + claim.getObjectValue() : claim.getNormalizedValue());
        return input;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, Object> parseJsonObject(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(rawJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private String extractString(String rawJson, String... keys) {
        Map<String, Object> values = parseJsonObject(rawJson);
        for (String key : keys) {
            Object value = values.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    private Long extractLong(String rawJson, String... keys) {
        Map<String, Object> values = parseJsonObject(rawJson);
        for (String key : keys) {
            Object value = values.get(key);
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String string && !string.isBlank()) {
                try {
                    return Long.parseLong(string);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private Double extractDouble(String rawJson, String... keys) {
        Map<String, Object> values = parseJsonObject(rawJson);
        for (String key : keys) {
            Object value = values.get(key);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
            if (value instanceof String string && !string.isBlank()) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
