package com.example.nexusa.SemanticValidation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfidenceScoringService {

    public double score(double ruleWeight,
                        Double sourceReliability,
                        Double entityConfidence,
                        double temporalPrecision,
                        double anomalyStrength,
                        double exceptionPenalty) {
        double score = clamp(ruleWeight)
                * defaulted(sourceReliability, 0.80)
                * defaulted(entityConfidence, 0.85)
                * clamp(temporalPrecision)
                * clamp(anomalyStrength)
                * clamp(exceptionPenalty);
        return Math.round(clamp(score) * 1000.0) / 1000.0;
    }

    public double deterministic(double ruleWeight, double temporalPrecision) {
        return score(ruleWeight, 0.92, 0.92, temporalPrecision, 1.0, 1.0);
    }

    private double defaulted(Double value, double fallback) {
        return value == null ? fallback : clamp(value);
    }

    private double clamp(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
