package com.example.nexusa.SemanticValidation.graph;

import com.example.nexusa.SemanticValidation.service.AiAnomalyDetector;
import com.example.nexusa.SemanticValidation.service.ConfidenceScoringService;
import com.example.nexusa.SemanticValidation.temporal.TemporalReasoner;

public record GraphValidationContext(
        HistoricalGraph graph,
        TemporalReasoner temporalReasoner,
        ConfidenceScoringService confidenceScoringService,
        AiAnomalyDetector aiAnomalyDetector,
        boolean includeAiChecks
) {
}
