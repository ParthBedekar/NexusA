package com.example.nexusa.ConfidenceScoring.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConfidenceScoringModel {
    private ConfidenceScoringModel() {
    }

    public static Map<String, Double> defaultWeights() {
        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("institutionReputation", 0.12);
        weights.put("citationQuality", 0.14);
        weights.put("researcherReputation", 0.10);
        weights.put("consensusSimilarity", 0.14);
        weights.put("sourceReliability", 0.16);
        weights.put("moderatorApproval", 0.12);
        weights.put("historicalConsistency", 0.12);
        weights.put("evidenceQuality", 0.10);
        return weights;
    }
}
