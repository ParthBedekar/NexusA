package com.example.nexusa.ConflictDetection.service;

import com.example.nexusa.Model.Enums.GlobalEnums.ConflictType;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import com.example.nexusa.Model.GlobalModels.ConflictSeverityScore;
import com.example.nexusa.Model.GlobalModels.NormalizedClaimValue;

@Component
public class ConflictSeverityScorer {

    public ConflictSeverityScore score(ConflictType type, Collection<NormalizedClaimValue> values) {
        List<NormalizedClaimValue> list = values.stream().toList();
        return switch (type) {
            case DATE -> scoreDateConflict(list);
            case RULER -> fixed(ValidationSeverity.ERROR, 0.82, "Different rulers are asserted for the same subject and predicate.");
            case LOCATION -> fixed(ValidationSeverity.ERROR, 0.78, "Different locations are asserted for the same subject and predicate.");
            case RELATIONSHIP -> fixed(ValidationSeverity.ERROR, 0.84, "Different relationship targets are asserted for the same subject and predicate.");
            case EVENT -> fixed(ValidationSeverity.WARNING, 0.68, "Different event descriptions or references are asserted.");
            case FACT -> fixed(ValidationSeverity.WARNING, 0.60, "Different factual values are asserted.");
        };
    }

    private ConflictSeverityScore scoreDateConflict(List<NormalizedClaimValue> values) {
        List<Long> numbers = values.stream()
                .map(NormalizedClaimValue::numericValue)
                .filter(value -> value != null)
                .sorted()
                .toList();
        if (numbers.size() < 2) {
            return fixed(ValidationSeverity.WARNING, 0.58, "Different textual date claims are asserted.");
        }

        long spread = numbers.get(numbers.size() - 1) - numbers.get(0);
        if (spread > 100) {
            return fixed(ValidationSeverity.CRITICAL, 0.95, "Date claims differ by more than 100 years.");
        }
        if (spread > 10) {
            return fixed(ValidationSeverity.ERROR, 0.82, "Date claims differ by more than 10 years.");
        }
        if (spread > 1) {
            return fixed(ValidationSeverity.WARNING, 0.58, "Date claims differ by multiple years.");
        }
        return fixed(ValidationSeverity.WARNING, 0.42, "Date claims differ by one year.");
    }

    private ConflictSeverityScore fixed(ValidationSeverity severity, double score, String explanation) {
        double confidence = Math.min(0.98, Math.max(0.55, score + 0.08));
        return new ConflictSeverityScore(severity, score, confidence, explanation);
    }
}
