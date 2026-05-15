package com.example.nexusa.Model;

import com.example.nexusa.Model.Enums.GlobalEnums.BlockType;
import com.example.nexusa.Model.Enums.GlobalEnums.CitationQuality;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictType;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import java.util.List;
import java.util.Map;
import lombok.Data;

public class GlobalModels {

    public static record ConfidenceFactorScore(
            String factor,
            double score,
            String explanation
    ) {
    }

    public static record ConflictSeverityScore(
            ValidationSeverity severity,
            double severityScore,
            double confidenceScore,
            String explanation
    ) {
    }

    public static record NormalizedClaimValue(
            ConflictType conflictType,
            String rawValue,
            String normalizedValue,
            Long numericValue
    ) {
    }

    public static record SimilarityScore(
            double totalScore,
            double nameScore,
            double tokenScore,
            double editDistanceScore,
            double temporalScore,
            boolean aliasMatch,
            List<String> evidence
    ) {
    }

    @Data
    public static class Citation {
        private String source;
        private String author;
        private Integer year;
        private String url;
        private CitationQuality quality; // PRIMARY, SECONDARY, TERTIARY
    }

    @Data
    public static class CMENode {
        private String nodeType;      // VOLUME or ENTRY
        private String title;
        private Long startYear;
        private Long endYear;
        private List<ContentBlock> blocks;  // only for ENTRY type
    }

    @Data
    public static class ContentBlock {
        private BlockType blockType;
        private Map<String, Object> data;
        private List<Citation> citations;
    }

    @Data
    public static class SerializedNode {
        private CMENode data;
        private List<SerializedNode> children;
    }

    public static record AiAnomalySignal(
            double anomalyScore,
            String anomalyType,
            String explanation,
            List<String> suggestedChecks
    ) {
    }

    public static record HistoricalInterval(Long startYear, Long endYear) {
    
        public boolean hasBothBounds() {
            return startYear != null && endYear != null;
        }
    
        public boolean isInverted() {
            return hasBothBounds() && startYear > endYear;
        }
    
        public boolean overlaps(HistoricalInterval other) {
            if (other == null || startYear == null || endYear == null ||
                    other.startYear == null || other.endYear == null) {
                return false;
            }
            return startYear <= other.endYear && other.startYear <= endYear;
        }
    
        public long durationYears() {
            if (!hasBothBounds()) {
                return 0;
            }
            return endYear - startYear;
        }
    }

}
