package com.example.nexusa.SemanticValidation;

import com.example.nexusa.SemanticValidation.dto.HistoricalEntityNode;
import com.example.nexusa.SemanticValidation.dto.HistoricalRelationshipEdge;
import com.example.nexusa.SemanticValidation.dto.SemanticGraphValidationRequest;
import com.example.nexusa.SemanticValidation.dto.SemanticValidationResponse;
import com.example.nexusa.SemanticValidation.rules.ContradictoryGeographyRule;
import com.example.nexusa.SemanticValidation.rules.DisconnectedEntityRule;
import com.example.nexusa.SemanticValidation.rules.DuplicateEmpireRule;
import com.example.nexusa.SemanticValidation.rules.ImpossibleAgeRule;
import com.example.nexusa.SemanticValidation.rules.ImpossibleTimelineRule;
import com.example.nexusa.SemanticValidation.rules.InvalidDynastySuccessionRule;
import com.example.nexusa.SemanticValidation.rules.OverlappingRulersRule;
import com.example.nexusa.SemanticValidation.rules.SemanticValidationRule;
import com.example.nexusa.SemanticValidation.rules.SuspiciousClaimRule;
import com.example.nexusa.SemanticValidation.service.ConfidenceScoringService;
import com.example.nexusa.SemanticValidation.service.HeuristicAiAnomalyDetector;
import com.example.nexusa.SemanticValidation.service.SemanticValidationService;
import com.example.nexusa.SemanticValidation.temporal.TemporalReasoner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticValidationServiceTests {

    @Test
    void validateGraphFlagsCoreHistoricalContradictions() {
        SemanticValidationService service = service();

        HistoricalEntityNode empire = node("empire_a", "EMPIRE", "Test Empire");
        empire.setStartYear(500L);
        empire.setEndYear(400L);

        HistoricalEntityNode rulerA = node("ruler_a", "PERSON", "Ruler A");
        rulerA.setBirthYear(460L);
        rulerA.setDeathYear(600L);

        HistoricalEntityNode rulerB = node("ruler_b", "PERSON", "Ruler B");
        rulerB.setBirthYear(450L);
        rulerB.setDeathYear(570L);

        HistoricalRelationshipEdge reignA = edge("ruler_a", "empire_a", "RULED", 450L, 520L);
        reignA.setExclusive(true);

        HistoricalRelationshipEdge reignB = edge("ruler_b", "empire_a", "RULED", 500L, 530L);
        reignB.setExclusive(true);

        SemanticGraphValidationRequest request = new SemanticGraphValidationRequest();
        request.setIncludeAiChecks(false);
        request.getNodes().addAll(List.of(empire, rulerA, rulerB));
        request.getEdges().addAll(List.of(reignA, reignB));

        SemanticValidationResponse response = service.validateGraph(request);

        Set<String> violationTypes = response.getFindings().stream()
                .map(finding -> finding.getViolationType())
                .collect(Collectors.toSet());

        assertThat(response.isPassed()).isFalse();
        assertThat(violationTypes).contains("IMPOSSIBLE_TIMELINE", "IMPOSSIBLE_AGE", "OVERLAPPING_RULERS");
    }

    private SemanticValidationService service() {
        List<SemanticValidationRule> rules = List.of(
                new ImpossibleTimelineRule(),
                new ImpossibleAgeRule(),
                new OverlappingRulersRule(),
                new InvalidDynastySuccessionRule(),
                new ContradictoryGeographyRule(),
                new DuplicateEmpireRule(),
                new DisconnectedEntityRule(),
                new SuspiciousClaimRule()
        );
        return new SemanticValidationService(
                rules,
                new TemporalReasoner(),
                new ConfidenceScoringService(),
                new HeuristicAiAnomalyDetector(),
                null,
                null,
                null,
                new ObjectMapper()
        );
    }

    private HistoricalEntityNode node(String id, String type, String name) {
        HistoricalEntityNode node = new HistoricalEntityNode();
        node.setId(id);
        node.setType(type);
        node.setName(name);
        return node;
    }

    private HistoricalRelationshipEdge edge(String fromId, String toId, String type, Long startYear, Long endYear) {
        HistoricalRelationshipEdge edge = new HistoricalRelationshipEdge();
        edge.setFromId(fromId);
        edge.setToId(toId);
        edge.setType(type);
        edge.setStartYear(startYear);
        edge.setEndYear(endYear);
        return edge;
    }
}
