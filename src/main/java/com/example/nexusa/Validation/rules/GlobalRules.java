package com.example.nexusa.Validation.rules;

import com.example.nexusa.Dto.GlobalDTOs.HistoricalClaimInput;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalEntityNode;
import com.example.nexusa.Dto.GlobalDTOs.HistoricalRelationshipEdge;
import com.example.nexusa.Dto.GlobalDTOs.SemanticValidationFindingDTO;
import com.example.nexusa.Model.Civilization;
import com.example.nexusa.Model.ResearchSubmission;
import com.example.nexusa.Repository.GlobalRepositories.CivilizationRepository;
import com.example.nexusa.SemanticValidation.graph.GraphValidationContext;
import com.example.nexusa.Model.GlobalModels.AiAnomalySignal;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.*;
import com.example.nexusa.Validation.core.*;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import java.util.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

public class GlobalRules {

    @Component
    public static class ContradictoryGeographyRule implements SemanticValidationRule {
        private static final double SUSPICIOUS_DISTANCE_KM = 4500.0;
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_CONTRADICTORY_GEOGRAPHY";
        }
    
        @Override
        public String getDescription() {
            return "Flags distant capitals, territories, or location claims without contact evidence.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
            for (HistoricalRelationshipEdge edge : context.graph().edges()) {
                if (!isGeographic(edge.getType())) {
                    continue;
                }
                HistoricalEntityNode left = context.graph().findNode(edge.getFromId()).orElse(null);
                HistoricalEntityNode right = context.graph().findNode(edge.getToId()).orElse(null);
                if (left == null || right == null || !hasCoordinates(left) || !hasCoordinates(right)) {
                    continue;
                }
                double distance = haversineKm(left.getLatitude(), left.getLongitude(), right.getLatitude(), right.getLongitude());
                if (distance > SUSPICIOUS_DISTANCE_KM && !hasContactEvidence(edge)) {
                    findings.add(SemanticValidationFindingDTO.builder()
                            .ruleCode(getRuleCode())
                            .violationType("CONTRADICTORY_GEOGRAPHY")
                            .severity(ValidationSeverity.WARNING)
                            .confidence(0.78)
                            .entityId(edge.getFromId())
                            .message("Geographic relationship spans a very large distance without contact, conquest, colony, or trade evidence.")
                            .evidence(List.of("relationship=" + edge.getType(), "distanceKm=" + Math.round(distance)))
                            .metadata(Map.of("targetEntityId", edge.getToId()))
                            .build());
                }
            }
            return findings;
        }
    
        private boolean isGeographic(String type) {
            return "CAPITAL_OF".equalsIgnoreCase(type)
                    || "LOCATED_IN".equalsIgnoreCase(type)
                    || "CONTROLLED_TERRITORY".equalsIgnoreCase(type)
                    || "CONTROLLED".equalsIgnoreCase(type);
        }
    
        private boolean hasCoordinates(HistoricalEntityNode node) {
            return node.getLatitude() != null && node.getLongitude() != null;
        }
    
        private boolean hasContactEvidence(HistoricalRelationshipEdge edge) {
            if (edge.getAttributes() == null) {
                return false;
            }
            return Boolean.TRUE.equals(edge.getAttributes().get("conquest"))
                    || Boolean.TRUE.equals(edge.getAttributes().get("colony"))
                    || Boolean.TRUE.equals(edge.getAttributes().get("tradeRoute"))
                    || Boolean.TRUE.equals(edge.getAttributes().get("maritimeContact"));
        }
    
        private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
            double radius = 6371.0;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        }
    }

    @Component
    public static class DisconnectedEntityRule implements SemanticValidationRule {
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_DISCONNECTED_ENTITY";
        }
    
        @Override
        public String getDescription() {
            return "Detects isolated or underspecified historical entities in the graph.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
            for (HistoricalEntityNode node : context.graph().nodes()) {
                int degree = context.graph().incidentEdges(node.getId()).size();
                if (degree == 0) {
                    findings.add(finding(node.getId(), "Historical entity has no graph relationships.",
                            ValidationSeverity.INFO, List.of("type=" + node.getType(), "name=" + node.getName())));
                    continue;
                }
                if ("DYNASTY".equalsIgnoreCase(node.getType()) && context.graph().incoming(node.getId(), "MEMBER_OF_DYNASTY").isEmpty()) {
                    findings.add(finding(node.getId(), "Dynasty has no member rulers attached.",
                            ValidationSeverity.WARNING, List.of("name=" + node.getName())));
                }
                if ("BATTLE".equalsIgnoreCase(node.getType()) && context.graph().incoming(node.getId(), "FOUGHT_IN").isEmpty()) {
                    findings.add(finding(node.getId(), "Battle has no participating entities attached.",
                            ValidationSeverity.WARNING, List.of("name=" + node.getName())));
                }
            }
            return findings;
        }
    
        private SemanticValidationFindingDTO finding(String entityId, String message, ValidationSeverity severity, List<String> evidence) {
            return SemanticValidationFindingDTO.builder()
                    .ruleCode(getRuleCode())
                    .violationType("DISCONNECTED_HISTORICAL_ENTITY")
                    .severity(severity)
                    .confidence(severity == ValidationSeverity.INFO ? 0.62 : 0.76)
                    .entityId(entityId)
                    .message(message)
                    .evidence(evidence)
                    .metadata(Map.of())
                    .build();
        }
    }

    @Component
    public static class DuplicateEmpireRule implements SemanticValidationRule {
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_DUPLICATE_EMPIRE";
        }
    
        @Override
        public String getDescription() {
            return "Detects likely duplicate empire or polity entities using names, dates, capitals, and rulers.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<HistoricalEntityNode> polities = context.graph().nodes().stream()
                    .filter(node -> isPolity(node.getType()))
                    .toList();
    
            Set<SemanticValidationFindingDTO> findings = new HashSet<>();
            for (int i = 0; i < polities.size(); i++) {
                HistoricalEntityNode left = polities.get(i);
                for (int j = i + 1; j < polities.size(); j++) {
                    HistoricalEntityNode right = polities.get(j);
                    double similarity = duplicateScore(context, left, right);
                    if (similarity >= 0.82) {
                        findings.add(SemanticValidationFindingDTO.builder()
                                .ruleCode(getRuleCode())
                                .violationType("DUPLICATE_EMPIRE")
                                .severity(ValidationSeverity.WARNING)
                                .confidence(Math.round(similarity * 1000.0) / 1000.0)
                                .entityId(left.getId())
                                .message("Two polity records look like possible duplicates.")
                                .evidence(List.of("left=" + left.getName(), "right=" + right.getName(), "score=" + similarity))
                                .metadata(Map.of("duplicateCandidateId", right.getId()))
                                .build());
                    }
                }
            }
            return List.copyOf(findings);
        }
    
        private boolean isPolity(String type) {
            return "EMPIRE".equalsIgnoreCase(type)
                    || "KINGDOM".equalsIgnoreCase(type)
                    || "POLITY".equalsIgnoreCase(type)
                    || "CIVILIZATION".equalsIgnoreCase(type);
        }
    
        private double duplicateScore(GraphValidationContext context, HistoricalEntityNode left, HistoricalEntityNode right) {
            double nameSimilarity = nameSimilarity(context.graph().normalizedName(left), context.graph().normalizedName(right));
            double dateOverlap = context.temporalReasoner().overlaps(left.getStartYear(), left.getEndYear(), right.getStartYear(), right.getEndYear()) ? 1.0 : 0.0;
            double capitalOverlap = sharedIncoming(context, left, right, "CAPITAL_OF") ? 1.0 : 0.0;
            double rulerOverlap = sharedIncoming(context, left, right, "RULED") ? 1.0 : 0.0;
            double aliasOverlap = aliasOverlap(left, right) ? 1.0 : 0.0;
            return (0.35 * nameSimilarity) + (0.20 * dateOverlap) + (0.15 * capitalOverlap)
                    + (0.20 * rulerOverlap) + (0.10 * aliasOverlap);
        }
    
        private double nameSimilarity(String left, String right) {
            if (left.isBlank() || right.isBlank()) {
                return 0.0;
            }
            if (left.equals(right)) {
                return 1.0;
            }
            if (left.contains(right) || right.contains(left)) {
                return 0.82;
            }
            Set<String> leftTokens = Set.of(left.split(" "));
            Set<String> rightTokens = Set.of(right.split(" "));
            long common = leftTokens.stream().filter(rightTokens::contains).count();
            return common == 0 ? 0.0 : (double) common / Math.max(leftTokens.size(), rightTokens.size());
        }
    
        private boolean sharedIncoming(GraphValidationContext context, HistoricalEntityNode left, HistoricalEntityNode right, String type) {
            Set<String> leftSources = context.graph().incoming(left.getId(), type).stream()
                    .map(HistoricalRelationshipEdge::getFromId)
                    .collect(java.util.stream.Collectors.toSet());
            return context.graph().incoming(right.getId(), type).stream()
                    .map(HistoricalRelationshipEdge::getFromId)
                    .anyMatch(leftSources::contains);
        }
    
        private boolean aliasOverlap(HistoricalEntityNode left, HistoricalEntityNode right) {
            if (left.getAliases() == null || right.getAliases() == null) {
                return false;
            }
            Set<String> aliases = left.getAliases().stream().map(String::toLowerCase).collect(java.util.stream.Collectors.toSet());
            return right.getAliases().stream().map(String::toLowerCase).anyMatch(aliases::contains);
        }
    }

    @Component
    public static class ImpossibleAgeRule implements SemanticValidationRule {
        private static final long MAX_LIFESPAN = 125;
        private static final long SUSPICIOUS_RULE_AGE = 80;
        private static final long IMPOSSIBLE_RULE_AGE = 120;
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_IMPOSSIBLE_AGE";
        }
    
        @Override
        public String getDescription() {
            return "Detects impossible or suspicious ages at death and start of rule.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
    
            for (HistoricalEntityNode node : context.graph().nodes()) {
                if (!"PERSON".equalsIgnoreCase(node.getType()) && !"RULER".equalsIgnoreCase(node.getType())) {
                    continue;
                }
    
                Long lifespan = context.temporalReasoner().ageAt(node.getBirthYear(), node.getDeathYear());
                if (lifespan != null && lifespan > MAX_LIFESPAN) {
                    findings.add(finding(node.getId(), "Recorded lifespan exceeds plausible human bounds.",
                            ValidationSeverity.CRITICAL, 0.97, List.of("lifespan=" + lifespan)));
                }
    
                for (HistoricalRelationshipEdge ruled : context.graph().outgoing(node.getId(), "RULED")) {
                    Long ageAtRule = context.temporalReasoner().ageAt(node.getBirthYear(), ruled.getStartYear());
                    if (ageAtRule == null) {
                        continue;
                    }
                    if (ageAtRule < 0) {
                        findings.add(finding(node.getId(), "Ruler begins reign before birth.",
                                ValidationSeverity.CRITICAL, 0.99, List.of("ageAtReignStart=" + ageAtRule)));
                    } else if (ageAtRule > IMPOSSIBLE_RULE_AGE) {
                        findings.add(finding(node.getId(), "Ruler begins reign at an impossible age.",
                                ValidationSeverity.CRITICAL, 0.96, List.of("ageAtReignStart=" + ageAtRule)));
                    } else if (ageAtRule > SUSPICIOUS_RULE_AGE) {
                        findings.add(finding(node.getId(), "Ruler begins reign at an unusually high age.",
                                ValidationSeverity.WARNING, 0.74, List.of("ageAtReignStart=" + ageAtRule)));
                    }
                }
            }
    
            return findings;
        }
    
        private SemanticValidationFindingDTO finding(String entityId, String message, ValidationSeverity severity,
                                                     double confidence, List<String> evidence) {
            return SemanticValidationFindingDTO.builder()
                    .ruleCode(getRuleCode())
                    .violationType("IMPOSSIBLE_AGE")
                    .severity(severity)
                    .confidence(confidence)
                    .entityId(entityId)
                    .message(message)
                    .evidence(evidence)
                    .metadata(Map.of())
                    .build();
        }
    }

    @Component
    public static class ImpossibleTimelineRule implements SemanticValidationRule {
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_IMPOSSIBLE_TIMELINE";
        }
    
        @Override
        public String getDescription() {
            return "Detects inverted lifespans, reigns, entity date ranges, and relationship intervals.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
    
            for (HistoricalEntityNode node : context.graph().nodes()) {
                if (context.temporalReasoner().isInverted(node.getStartYear(), node.getEndYear())) {
                    findings.add(finding(node.getId(), "Entity start year is after end year.",
                            List.of("startYear=" + node.getStartYear(), "endYear=" + node.getEndYear()),
                            context.confidenceScoringService().deterministic(0.98, 1.0)));
                }
                if (context.temporalReasoner().isInverted(node.getBirthYear(), node.getDeathYear())) {
                    findings.add(finding(node.getId(), "Person death year is before birth year.",
                            List.of("birthYear=" + node.getBirthYear(), "deathYear=" + node.getDeathYear()),
                            context.confidenceScoringService().deterministic(0.99, 1.0)));
                }
            }
    
            for (HistoricalRelationshipEdge edge : context.graph().edges()) {
                if (context.temporalReasoner().isInverted(edge.getStartYear(), edge.getEndYear())) {
                    findings.add(SemanticValidationFindingDTO.builder()
                            .ruleCode(getRuleCode())
                            .violationType("IMPOSSIBLE_TIMELINE")
                            .severity(ValidationSeverity.CRITICAL)
                            .confidence(context.confidenceScoringService().deterministic(0.98, 1.0))
                            .entityId(edge.getFromId())
                            .message("Relationship interval is inverted.")
                            .evidence(List.of("edge=" + edge.getType(), "startYear=" + edge.getStartYear(), "endYear=" + edge.getEndYear()))
                            .metadata(Map.of("edgeId", edge.getId() == null ? "" : edge.getId(), "targetEntityId", edge.getToId()))
                            .build());
                }
            }
    
            return findings;
        }
    
        private SemanticValidationFindingDTO finding(String entityId, String message, List<String> evidence, double confidence) {
            return SemanticValidationFindingDTO.builder()
                    .ruleCode(getRuleCode())
                    .violationType("IMPOSSIBLE_TIMELINE")
                    .severity(ValidationSeverity.CRITICAL)
                    .confidence(confidence)
                    .entityId(entityId)
                    .message(message)
                    .evidence(evidence)
                    .metadata(Map.of())
                    .build();
        }
    }

    @Component
    public static class InvalidDynastySuccessionRule implements SemanticValidationRule {
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_INVALID_DYNASTY_SUCCESSION";
        }
    
        @Override
        public String getDescription() {
            return "Checks succession edges for polity mismatch, temporal reversal, and unexplained dynasty switches.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
    
            for (HistoricalRelationshipEdge succession : context.graph().edgesOfType("SUCCEEDED")) {
                String predecessor = succession.getFromId();
                String successor = succession.getToId();
                Optional<HistoricalRelationshipEdge> predecessorReign = firstReign(context, predecessor);
                Optional<HistoricalRelationshipEdge> successorReign = firstReign(context, successor);
    
                if (predecessorReign.isEmpty() || successorReign.isEmpty()) {
                    findings.add(finding(predecessor, "Succession is disconnected from one or both reign records.",
                            ValidationSeverity.WARNING, List.of("predecessor=" + predecessor, "successor=" + successor),
                            metadata("successor", successor)));
                    continue;
                }
    
                if (!predecessorReign.get().getToId().equals(successorReign.get().getToId())) {
                    findings.add(finding(predecessor, "Successor and predecessor rule different polities.",
                            ValidationSeverity.ERROR, List.of("predecessorPolity=" + predecessorReign.get().getToId(),
                                    "successorPolity=" + successorReign.get().getToId()),
                            metadata("successor", successor)));
                }
    
                if (context.temporalReasoner().overlaps(predecessorReign.get().getStartYear(), predecessorReign.get().getEndYear(),
                        successorReign.get().getStartYear(), successorReign.get().getEndYear()) &&
                        !hasTransitionException(succession)) {
                    findings.add(finding(predecessor, "Succession overlaps without co-rule, contested rule, or interregnum metadata.",
                            ValidationSeverity.ERROR, List.of("predecessorEnd=" + predecessorReign.get().getEndYear(),
                                    "successorStart=" + successorReign.get().getStartYear()),
                            metadata("successor", successor)));
                }
    
                String predecessorDynasty = dynastyOf(context, predecessor);
                String successorDynasty = dynastyOf(context, successor);
                if (predecessorDynasty != null && successorDynasty != null &&
                        !predecessorDynasty.equals(successorDynasty) && !hasTransitionException(succession)) {
                    findings.add(finding(predecessor, "Dynasty changes across succession without transition evidence.",
                            ValidationSeverity.WARNING, List.of("predecessorDynasty=" + predecessorDynasty,
                                    "successorDynasty=" + successorDynasty),
                            metadata("successor", successor)));
                }
            }
    
            return findings;
        }
    
        private Optional<HistoricalRelationshipEdge> firstReign(GraphValidationContext context, String rulerId) {
            return context.graph().outgoing(rulerId, "RULED").stream().findFirst();
        }
    
        private String dynastyOf(GraphValidationContext context, String rulerId) {
            return context.graph().outgoing(rulerId, "MEMBER_OF_DYNASTY").stream()
                    .map(HistoricalRelationshipEdge::getToId)
                    .findFirst()
                    .orElse(null);
        }
    
        private boolean hasTransitionException(HistoricalRelationshipEdge edge) {
            if (edge.getAttributes() == null) {
                return false;
            }
            return Boolean.TRUE.equals(edge.getAttributes().get("transitionEvidence"))
                    || Boolean.TRUE.equals(edge.getAttributes().get("contested"))
                    || Boolean.TRUE.equals(edge.getAttributes().get("coregency"))
                    || Boolean.TRUE.equals(edge.getAttributes().get("conquest"))
                    || Boolean.TRUE.equals(edge.getAttributes().get("interregnum"));
        }
    
        private SemanticValidationFindingDTO finding(String entityId, String message, ValidationSeverity severity,
                                                     List<String> evidence, Map<String, Object> metadata) {
            return SemanticValidationFindingDTO.builder()
                    .ruleCode(getRuleCode())
                    .violationType("INVALID_DYNASTY_SUCCESSION")
                    .severity(severity)
                    .confidence(severity == ValidationSeverity.ERROR ? 0.84 : 0.70)
                    .entityId(entityId)
                    .message(message)
                    .evidence(evidence)
                    .metadata(metadata)
                    .build();
        }
    
        private Map<String, Object> metadata(String key, Object value) {
            Map<String, Object> metadata = new HashMap<>();
            if (value != null) {
                metadata.put(key, value);
            }
            return metadata;
        }
    }

    @Component
    public static class OverlappingRulersRule implements SemanticValidationRule {
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_OVERLAPPING_RULERS";
        }
    
        @Override
        public String getDescription() {
            return "Detects overlapping exclusive rulers over the same polity.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
            List<HistoricalRelationshipEdge> reigns = new ArrayList<>(context.graph().edgesOfType("RULED"));
            reigns.sort(Comparator.comparing(HistoricalRelationshipEdge::getToId, Comparator.nullsLast(String::compareTo))
                    .thenComparing(HistoricalRelationshipEdge::getStartYear, Comparator.nullsLast(Long::compareTo)));
    
            for (int i = 0; i < reigns.size(); i++) {
                HistoricalRelationshipEdge left = reigns.get(i);
                for (int j = i + 1; j < reigns.size(); j++) {
                    HistoricalRelationshipEdge right = reigns.get(j);
                    if (!samePolity(left, right) || sameRuler(left, right)) {
                        continue;
                    }
                    if (!context.temporalReasoner().overlaps(left.getStartYear(), left.getEndYear(), right.getStartYear(), right.getEndYear())) {
                        continue;
                    }
                    if (hasOverlapException(left) || hasOverlapException(right)) {
                        continue;
                    }
                    findings.add(SemanticValidationFindingDTO.builder()
                            .ruleCode(getRuleCode())
                            .violationType("OVERLAPPING_RULERS")
                            .severity(ValidationSeverity.ERROR)
                            .confidence(context.confidenceScoringService().deterministic(0.93, 1.0))
                            .entityId(left.getToId())
                            .message("Two exclusive rulers overlap over the same polity without an exception marker.")
                            .evidence(List.of(
                                    "rulerA=" + left.getFromId() + " " + left.getStartYear() + ".." + left.getEndYear(),
                                    "rulerB=" + right.getFromId() + " " + right.getStartYear() + ".." + right.getEndYear()))
                            .metadata(metadata(left, right))
                            .build());
                }
            }
    
            return findings;
        }
    
        private boolean samePolity(HistoricalRelationshipEdge left, HistoricalRelationshipEdge right) {
            return left.getToId() != null && left.getToId().equals(right.getToId());
        }
    
        private boolean sameRuler(HistoricalRelationshipEdge left, HistoricalRelationshipEdge right) {
            return left.getFromId() != null && left.getFromId().equals(right.getFromId());
        }
    
        private boolean hasOverlapException(HistoricalRelationshipEdge edge) {
            if (Boolean.FALSE.equals(edge.getExclusive())) {
                return true;
            }
            Object exception = edge.getAttributes() == null ? null : edge.getAttributes().get("overlapException");
            return exception instanceof Boolean b && b;
        }
    
        private Map<String, Object> metadata(HistoricalRelationshipEdge left, HistoricalRelationshipEdge right) {
            Map<String, Object> metadata = new HashMap<>();
            if (left.getFromId() != null) {
                metadata.put("rulerA", left.getFromId());
            }
            if (right.getFromId() != null) {
                metadata.put("rulerB", right.getFromId());
            }
            return metadata;
        }
    }

    public interface SemanticValidationRule {
        String getRuleCode();
    
        String getDescription();
    
        List<SemanticValidationFindingDTO> validate(GraphValidationContext context);
    }

    @Component
    public static class SuspiciousClaimRule implements SemanticValidationRule {
    
        @Override
        public String getRuleCode() {
            return "SEMANTIC_SUSPICIOUS_CLAIM";
        }
    
        @Override
        public String getDescription() {
            return "Validates historical claims for anachronism, unsupported relationship claims, and AI-assisted semantic anomalies.";
        }
    
        @Override
        public List<SemanticValidationFindingDTO> validate(GraphValidationContext context) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
            for (HistoricalClaimInput claim : context.graph().claims()) {
                findings.addAll(validateExistenceWindow(context, claim));
                findings.addAll(validateRelationshipSupport(context, claim));
                if (context.includeAiChecks()) {
                    findings.addAll(validateAiSignal(context, claim));
                }
            }
            return findings;
        }
    
        private List<SemanticValidationFindingDTO> validateExistenceWindow(GraphValidationContext context, HistoricalClaimInput claim) {
            List<SemanticValidationFindingDTO> findings = new ArrayList<>();
            if (claim.getStartYear() == null) {
                return findings;
            }
            List<String> entityIds = new ArrayList<>();
            if (claim.getSubjectEntityId() != null) {
                entityIds.add(claim.getSubjectEntityId());
            }
            if (claim.getObjectEntityId() != null) {
                entityIds.add(claim.getObjectEntityId());
            }
            for (String entityId : entityIds) {
                if (entityId == null) {
                    continue;
                }
                HistoricalEntityNode entity = context.graph().findNode(entityId).orElse(null);
                if (entity == null) {
                    continue;
                }
                boolean before = entity.getStartYear() != null && claim.getStartYear() < entity.getStartYear();
                boolean after = entity.getEndYear() != null && claim.getStartYear() > entity.getEndYear();
                if (before || after) {
                    findings.add(SemanticValidationFindingDTO.builder()
                            .ruleCode(getRuleCode())
                            .violationType("ANACHRONISTIC_CLAIM")
                            .severity(ValidationSeverity.ERROR)
                            .confidence(context.confidenceScoringService().score(0.94, claim.getSourceReliability(),
                                    claim.getConfidence(), context.temporalReasoner().precisionFactor(claim.getStartYear(), claim.getEndYear()), 1.0, 1.0))
                            .entityId(entityId)
                            .claimId(claim.getId())
                            .message("Claim references an entity outside its known existence window.")
                            .evidence(List.of("claimYear=" + claim.getStartYear(),
                                    "entityWindow=" + entity.getStartYear() + ".." + entity.getEndYear()))
                            .metadata(Map.of("predicate", claim.getPredicate() == null ? "" : claim.getPredicate()))
                            .build());
                }
            }
            return findings;
        }
    
        private List<SemanticValidationFindingDTO> validateRelationshipSupport(GraphValidationContext context, HistoricalClaimInput claim) {
            if (claim.getSubjectEntityId() == null || claim.getObjectEntityId() == null || claim.getPredicate() == null) {
                return List.of();
            }
            if (!isRelationshipPredicate(claim.getPredicate())) {
                return List.of();
            }
            boolean directlySupported = context.graph().edgesBetween(claim.getSubjectEntityId(), claim.getObjectEntityId()).stream()
                    .anyMatch(edge -> claim.getPredicate().equalsIgnoreCase(edge.getType()));
            if (directlySupported) {
                return List.of();
            }
            return List.of(SemanticValidationFindingDTO.builder()
                    .ruleCode(getRuleCode())
                    .violationType("UNSUPPORTED_HISTORICAL_CLAIM")
                    .severity(ValidationSeverity.WARNING)
                    .confidence(context.confidenceScoringService().score(0.78, claim.getSourceReliability(), claim.getConfidence(), 0.8, 1.0, 1.0))
                    .entityId(claim.getSubjectEntityId())
                    .claimId(claim.getId())
                    .message("Relationship claim has no matching support edge in the historical graph.")
                    .evidence(List.of("predicate=" + claim.getPredicate(), "objectEntityId=" + claim.getObjectEntityId()))
                    .metadata(Map.of())
                    .build());
        }
    
        private List<SemanticValidationFindingDTO> validateAiSignal(GraphValidationContext context, HistoricalClaimInput claim) {
            AiAnomalySignal signal = context.aiAnomalyDetector().inspectClaim(claim, context.graph());
            if (signal.anomalyScore() < 0.75) {
                return List.of();
            }
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("suggestedChecks", signal.suggestedChecks());
            return List.of(SemanticValidationFindingDTO.builder()
                    .ruleCode(getRuleCode())
                    .violationType(signal.anomalyType().toUpperCase())
                    .severity(signal.anomalyScore() >= 0.9 ? ValidationSeverity.ERROR : ValidationSeverity.WARNING)
                    .confidence(signal.anomalyScore())
                    .entityId(claim.getSubjectEntityId())
                    .claimId(claim.getId())
                    .message(signal.explanation())
                    .evidence(List.of("aiAnomalyScore=" + signal.anomalyScore()))
                    .metadata(metadata)
                    .build());
        }
    
        private boolean isRelationshipPredicate(String predicate) {
            return "CONQUERED".equalsIgnoreCase(predicate)
                    || "RULED".equalsIgnoreCase(predicate)
                    || "CONTROLLED".equalsIgnoreCase(predicate)
                    || "CAPITAL_OF".equalsIgnoreCase(predicate)
                    || "LOCATED_IN".equalsIgnoreCase(predicate)
                    || "PART_OF".equalsIgnoreCase(predicate);
        }
    }

    /**
     * Rule: CHRONOLOGY
     * Verifies that the referenced civilization's start date is before its end date,
     * and that the submission's date claims do not fall outside the civilization's temporal bounds.
     */
    @Component
    public static class ChronologyRule implements ValidationRule {
    
        @Override
        public String getRuleCode() { return "CHRONOLOGY"; }
    
        @Override
        public String getDescription() { return "Validates that date sequences are logically ordered and within civilization temporal bounds"; }
    
        @Override
        public List<ValidationError> validate(ValidationContext context) {
            List<ValidationError> errors = new ArrayList<>();
    
            if (context.getSubmission().getCivilizationReference() == null) {
                return errors; // handled by RequiredFieldsRule
            }
    
            Civilization civ = context.getSubmission().getCivilizationReference();
            Long startDate = civ.getStartDate();
            Long endDate = civ.getEndDate();
    
            // Both must be present for a meaningful chronology check
            if (startDate != null && endDate != null && startDate >= endDate) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("civilizationReference.startDate / endDate")
                        .message("Civilization start date (" + startDate + ") must be earlier than end date (" + endDate + ")")
                        .severity(ValidationSeverity.ERROR)
                        .rejectedValue("startDate=" + startDate + ", endDate=" + endDate)
                        .build());
            }
    
            // Check payload-level date claims if parsed payload is available
            if (context.getParsedPayload() != null) {
                Object payloadStart = context.getParsedPayload().get("startDate");
                Object payloadEnd = context.getParsedPayload().get("endDate");
    
                if (payloadStart instanceof Number s && payloadEnd instanceof Number e) {
                    if (s.longValue() >= e.longValue()) {
                        errors.add(ValidationError.builder()
                                .ruleCode(getRuleCode())
                                .fieldPath("rawStructuredPayload.startDate / endDate")
                                .message("Payload startDate must be earlier than endDate")
                                .severity(ValidationSeverity.ERROR)
                                .rejectedValue("startDate=" + s + ", endDate=" + e)
                                .build());
                    }
                }
            }
    
            return errors;
        }
    }

    /**
     * Rule: CITATION_FORMAT
     * Validates that citation references in the payload follow acceptable formats.
     * Checks DOI syntax (10.XXXX/...) and basic URL well-formedness.
     */
    @Component
    public static class CitationFormatRule implements ValidationRule {
    
        // Standard DOI pattern: starts with 10. followed by registrant and suffix
        private static final Pattern DOI_PATTERN = Pattern.compile("^10\\.\\d{4,9}/[-._;()/:a-zA-Z0-9]+$");
    
        // Very basic URL pattern — must start with http:// or https://
        private static final Pattern URL_PATTERN = Pattern.compile("^https?://.+");
    
        @Override
        public String getRuleCode() { return "CITATION_FORMAT"; }
    
        @Override
        public String getDescription() { return "Validates DOI format and URL well-formedness in citation references"; }
    
        @Override
        public List<ValidationError> validate(ValidationContext context) {
            List<ValidationError> errors = new ArrayList<>();
            Map<String, Object> payload = context.getParsedPayload();
            if (payload == null) return errors;
    
            // Check top-level DOI and URL fields in the payload
            validateDoi(payload.get("doi"), "rawStructuredPayload.doi", errors);
            validateUrl(payload.get("url"), "rawStructuredPayload.url", errors);
    
            // Check nested citations array if present
            Object citations = payload.get("citations");
            if (citations instanceof List<?> citationList) {
                for (int i = 0; i < citationList.size(); i++) {
                    Object item = citationList.get(i);
                    if (item instanceof Map<?, ?> citation) {
                        validateDoi(citation.get("doi"), "rawStructuredPayload.citations[" + i + "].doi", errors);
                        validateUrl(citation.get("url"), "rawStructuredPayload.citations[" + i + "].url", errors);
                    }
                }
            }
    
            return errors;
        }
    
        private void validateDoi(Object value, String path, List<ValidationError> errors) {
            if (value == null) return;
            String doi = value.toString().trim();
            if (!doi.isBlank() && !DOI_PATTERN.matcher(doi).matches()) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath(path)
                        .message("Invalid DOI format. Expected pattern: 10.XXXX/suffix, found: " + doi)
                        .severity(ValidationSeverity.WARNING)
                        .rejectedValue(doi)
                        .build());
            }
        }
    
        private void validateUrl(Object value, String path, List<ValidationError> errors) {
            if (value == null) return;
            String url = value.toString().trim();
            if (!url.isBlank() && !URL_PATTERN.matcher(url).matches()) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath(path)
                        .message("Malformed URL. Must start with http:// or https://")
                        .severity(ValidationSeverity.WARNING)
                        .rejectedValue(url)
                        .build());
            }
        }
    }

    /**
     * Rule: CIVILIZATION_REFERENCE
     * Verifies that the civilization referenced in the submission actually exists in the database.
     */
    @Component
    public static class CivilizationReferenceRule implements ValidationRule {
    
        private final CivilizationRepository civilizationRepository;
    
        public CivilizationReferenceRule(CivilizationRepository civilizationRepository) {
            this.civilizationRepository = civilizationRepository;
        }
    
        @Override
        public String getRuleCode() { return "CIVILIZATION_REFERENCE"; }
    
        @Override
        public String getDescription() { return "Validates that the civilization reference exists in the database"; }
    
        @Override
        public List<ValidationError> validate(ValidationContext context) {
            List<ValidationError> errors = new ArrayList<>();
    
            if (context.getSubmission().getCivilizationReference() == null) {
                return errors; // handled by RequiredFieldsRule
            }
    
            UUID civId = context.getSubmission().getCivilizationReference().getCivId();
            if (civId == null || civilizationRepository.findById(civId).isEmpty()) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("civilizationReference")
                        .message("Civilization with ID '" + civId + "' does not exist")
                        .severity(ValidationSeverity.CRITICAL)
                        .rejectedValue(civId != null ? civId.toString() : "null")
                        .build());
            }
    
            return errors;
        }
    }

    /**
     * Rule: DATE_RANGE
     * Validates that date values in the payload fall within academically plausible ranges.
     * Rejects dates earlier than 100,000 BCE or later than the current year.
     */
    @Component
    public static class DateRangeRule implements ValidationRule {
    
        private static final long MIN_ALLOWED_DATE = -100_000L; // 100,000 BCE
        private static final long MAX_ALLOWED_DATE = 2100L;     // Reasonable ceiling
    
        @Override
        public String getRuleCode() { return "DATE_RANGE"; }
    
        @Override
        public String getDescription() { return "Validates that date values fall within plausible historical boundaries"; }
    
        @Override
        public List<ValidationError> validate(ValidationContext context) {
            List<ValidationError> errors = new ArrayList<>();
            Map<String, Object> payload = context.getParsedPayload();
    
            if (payload == null) return errors;
    
            checkDate(payload, "startDate", errors);
            checkDate(payload, "endDate", errors);
    
            return errors;
        }
    
        private void checkDate(Map<String, Object> payload, String field, List<ValidationError> errors) {
            Object raw = payload.get(field);
            if (raw == null) return;
    
            if (!(raw instanceof Number)) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("rawStructuredPayload." + field)
                        .message("Field '" + field + "' must be a numeric year value")
                        .severity(ValidationSeverity.ERROR)
                        .rejectedValue(raw.toString())
                        .build());
                return;
            }
    
            long year = ((Number) raw).longValue();
            if (year < MIN_ALLOWED_DATE || year > MAX_ALLOWED_DATE) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("rawStructuredPayload." + field)
                        .message("Date " + year + " is outside the allowed range [" + MIN_ALLOWED_DATE + ", " + MAX_ALLOWED_DATE + "]")
                        .severity(ValidationSeverity.ERROR)
                        .rejectedValue(String.valueOf(year))
                        .build());
            }
        }
    }

    /**
     * Rule: DUPLICATE_NODE_NAME
     * Detects duplicate node names within the submitted tree payload.
     * Nodes with identical names at the same hierarchy level suggest copy-paste errors.
     */
    @Component
    public static class DuplicateNodeNameRule implements ValidationRule {
    
        @Override
        public String getRuleCode() { return "DUPLICATE_NODE_NAME"; }
    
        @Override
        public String getDescription() { return "Detects duplicate node names within the submission payload tree structure"; }
    
        @Override
        public List<ValidationError> validate(ValidationContext context) {
            List<ValidationError> errors = new ArrayList<>();
            Map<String, Object> payload = context.getParsedPayload();
            if (payload == null) return errors;
    
            // Collect all node names from the nodes array in the payload
            Object nodesRaw = payload.get("nodes");
            if (!(nodesRaw instanceof List<?> nodes)) return errors;
    
            Set<String> seen = new HashSet<>();
            Set<String> duplicates = new LinkedHashSet<>();
    
            for (Object nodeObj : nodes) {
                if (nodeObj instanceof Map<?, ?> node) {
                    Object nameObj = node.get("name");
                    if (nameObj instanceof String name) {
                        String normalized = name.trim().toLowerCase();
                        if (!seen.add(normalized)) {
                            duplicates.add(name.trim());
                        }
                    }
                }
            }
    
            for (String dup : duplicates) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("rawStructuredPayload.nodes[].name")
                        .message("Duplicate node name found: '" + dup + "'. Node names must be unique within the submission.")
                        .severity(ValidationSeverity.ERROR)
                        .rejectedValue(dup)
                        .build());
            }
    
            return errors;
        }
    }

    /**
     * Rule: METADATA_STRUCTURE
     * Validates that the metadata object in the payload contains only known keys
     * and that required metadata fields are present.
     */
    @Component
    public static class MetadataStructureRule implements ValidationRule {
    
        private static final Set<String> REQUIRED_METADATA_KEYS = Set.of("source", "language");
        private static final Set<String> KNOWN_METADATA_KEYS = Set.of(
                "source", "language", "region", "period", "tags",
                "confidence", "archaeologicalSite", "dynasty"
        );
    
        @Override
        public String getRuleCode() { return "METADATA_STRUCTURE"; }
    
        @Override
        public String getDescription() { return "Validates that metadata keys are known and required metadata fields are present"; }
    
        @Override
        public List<ValidationError> validate(ValidationContext context) {
            List<ValidationError> errors = new ArrayList<>();
            Map<String, Object> payload = context.getParsedPayload();
            if (payload == null) return errors;
    
            Object metaRaw = payload.get("metadata");
            if (metaRaw == null) {
                // Metadata block is optional but recommended
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("rawStructuredPayload.metadata")
                        .message("No metadata block found. A metadata block with at least 'source' and 'language' is recommended")
                        .severity(ValidationSeverity.WARNING)
                        .build());
                return errors;
            }
    
            if (!(metaRaw instanceof Map<?, ?> meta)) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("rawStructuredPayload.metadata")
                        .message("Metadata must be a JSON object, not a primitive or array")
                        .severity(ValidationSeverity.ERROR)
                        .rejectedValue(metaRaw.toString())
                        .build());
                return errors;
            }
    
            // Check required keys
            for (String required : REQUIRED_METADATA_KEYS) {
                if (!meta.containsKey(required)) {
                    errors.add(ValidationError.builder()
                            .ruleCode(getRuleCode())
                            .fieldPath("rawStructuredPayload.metadata." + required)
                            .message("Required metadata field '" + required + "' is missing")
                            .severity(ValidationSeverity.WARNING)
                            .build());
                }
            }
    
            // Warn on unknown keys
            for (Object key : meta.keySet()) {
                if (key instanceof String k && !KNOWN_METADATA_KEYS.contains(k)) {
                    errors.add(ValidationError.builder()
                            .ruleCode(getRuleCode())
                            .fieldPath("rawStructuredPayload.metadata." + k)
                            .message("Unknown metadata key '" + k + "'. Only known keys are indexed for search")
                            .severity(ValidationSeverity.INFO)
                            .rejectedValue(k)
                            .build());
                }
            }
    
            return errors;
        }
    }

    /**
     * Rule: REQUIRED_FIELDS
     * Checks that all mandatory fields on the submission are non-null and non-blank.
     * This is a short-circuit rule — if it fires CRITICAL, the pipeline stops.
     */
    @Component
    public static class RequiredFieldsRule implements ValidationRule {
    
        @Override
        public String getRuleCode() { return "REQUIRED_FIELDS"; }
    
        @Override
        public String getDescription() { return "Validates that all mandatory submission fields are present and non-empty"; }
    
        @Override
        public boolean isShortCircuit() { return true; }
    
        @Override
        public List<ValidationError> validate(ValidationContext context) {
            List<ValidationError> errors = new ArrayList<>();
            ResearchSubmission sub = context.getSubmission();
    
            if (sub.getSubmissionTitle() == null || sub.getSubmissionTitle().isBlank()) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("submissionTitle")
                        .message("Submission title is required and cannot be blank")
                        .severity(ValidationSeverity.CRITICAL)
                        .build());
            }
    
            if (sub.getCivilizationReference() == null) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("civilizationReference")
                        .message("A civilization reference is required")
                        .severity(ValidationSeverity.CRITICAL)
                        .build());
            }
    
            if (sub.getResearcher() == null) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("researcher")
                        .message("Researcher identity is missing")
                        .severity(ValidationSeverity.CRITICAL)
                        .build());
            }
    
            if (sub.getInstitution() == null) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("institution")
                        .message("Institution is required")
                        .severity(ValidationSeverity.CRITICAL)
                        .build());
            }
    
            if (sub.getRawStructuredPayload() == null || sub.getRawStructuredPayload().isBlank()) {
                errors.add(ValidationError.builder()
                        .ruleCode(getRuleCode())
                        .fieldPath("rawStructuredPayload")
                        .message("Structured payload cannot be empty")
                        .severity(ValidationSeverity.ERROR)
                        .build());
            }
    
            return errors;
        }
    }

}
