package com.example.nexusa.Dto;

import com.example.nexusa.Model.GlobalModels.CMENode;
import com.example.nexusa.Model.Enums.GlobalEnums.AliasType;
import com.example.nexusa.Model.Enums.GlobalEnums.CanonicalEntityStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimType;
import com.example.nexusa.Model.Enums.GlobalEnums.CommitType;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictGroupStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictType;
import com.example.nexusa.Model.Enums.GlobalEnums.DuplicateCandidateStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.EvidenceType;
import com.example.nexusa.Model.Enums.GlobalEnums.ResearchSubmissionStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.Role;
import com.example.nexusa.Moderation.entity.ModerationTask.TargetType;
import com.example.nexusa.Moderation.entity.ModerationTask.TaskPriority;
import com.example.nexusa.Moderation.entity.ModerationTask.TaskStatus;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationError;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationReport;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationResult;
import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Value;
import com.example.nexusa.Model.GlobalModels.Citation;

public class GlobalDTOs {

    @Data
    public static class ConfidenceScoreRequest {
        private UUID claimId;
        private Integer scoringVersion;
        private UUID calculatedBy;
        private boolean persistScore = true;
        private boolean updateClaimConfidence = true;
    }

    @Value
    @Builder
    public static class ConfidenceScoreResponseDTO {
        UUID scoreId;
        UUID claimId;
        double score;
        String confidenceLabel;
        Integer scoringVersion;
        Map<String, Double> factorScores;
        List<String> explanation;
        LocalDateTime calculatedAt;
    }

    @Data
    public static class RecalculateScoresRequest {
        private List<UUID> claimIds;
        private Integer scoringVersion;
        private UUID calculatedBy;
        private boolean updateClaimConfidence = true;
    }

    @Data
    public static class ScoringWeightsDTO {
        private Integer version;
        private String name;
        private Map<String, Double> weights;
        private UUID createdBy;
    }

    @Data
    public static class ConflictDetectionRequest {
        private UUID subjectEntityId;
        private String predicate;
        private boolean persistGroups = true;
        private double minimumSeverityScore = 0.35;
    }

    @Value
    @Builder
    public static class ConflictDetectionResponse {
        int totalGroups;
        int persistedGroups;
        List<ConflictGroupResponseDTO> groups;
    }

    @Value
    @Builder
    public static class ConflictGroupResponseDTO {
        UUID conflictGroupId;
        UUID subjectEntityId;
        String subjectEntityType;
        String predicate;
        ConflictType conflictType;
        ValidationSeverity severity;
        double severityScore;
        double confidenceScore;
        String summary;
        List<String> conflictingValues;
        List<UUID> claimIds;
        ConflictGroupStatus status;
        UUID resolvedClaimId;
        LocalDateTime createdAt;
    }

    @Data
    public static class ConflictModerationRequest {
        private ConflictGroupStatus status;
        private UUID reviewedBy;
        private UUID resolvedClaimId;
        private String reviewNotes;
    }

    @Data
    public static class AddNodeRequestDTO {
        private UUID parentNodeId;
        private CMENode node;
        private String commitMsg;
        private CommitType commitType;
    }

    @Data
    public static class CitationSourceCreateDTO {
    
        @NotNull(message = "Evidence type is required")
        private EvidenceType evidenceType;
    
        @NotBlank(message = "Title is required")
        private String title;
    
        private String authors;
        private Integer publicationYear;
        private String publisher;
        private String journalName;
        private String doi;
        private String isbn;
        private String url;
        private String archiveName;
        private String archiveLocation;
        private String extendedMetadata; // JSON string
    
        @DecimalMin(value = "0.0", message = "Reliability score must be between 0.0 and 1.0")
        @DecimalMax(value = "1.0", message = "Reliability score must be between 0.0 and 1.0")
        private Double reliabilityScore = 0.5;
    }

    @Data
    public static class CitationSourceResponseDTO {
        private UUID citationId;
        private EvidenceType evidenceType;
        private String title;
        private String authors;
        private Integer publicationYear;
        private String publisher;
        private String journalName;
        private String doi;
        private String isbn;
        private String url;
        private String archiveName;
        private String archiveLocation;
        private String extendedMetadata;
        private Double reliabilityScore;
        private UUID submittedById;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ClaimEvidenceCreateDTO {
    
        @NotNull(message = "Claim ID is required")
        private UUID claimId;
    
        @NotNull(message = "Citation Source ID is required")
        private UUID citationSourceId;
    
        @NotBlank(message = "Evidence text is required")
        private String evidenceText;
    
        private String quotedPassage;
        private String pageNumbers;
        private String extractedMetadata; // JSON string
        private String uploadedDocuments; // Comma-separated file paths or JSON array
        private Double sourceReliabilityScore; // Optional override; defaults to source's own score
    }

    @Data
    public static class ClaimEvidenceResponseDTO {
        private UUID evidenceId;
        private UUID claimId;
        private UUID citationSourceId;
        private String citationTitle;
        private EvidenceType evidenceType;
        private String evidenceText;
        private String quotedPassage;
        private String pageNumbers;
        private String extractedMetadata;
        private String uploadedDocuments;
        private Double sourceReliabilityScore;
        private UUID submittedById;
        private LocalDateTime createdAt;
    }

    @Data
    public static class CreateCivilizationDTO {
        private String title;
        private String description;
        private Long startYear;
        private Long endYear;
        private String commitMsg;
    }

    @Data
    public static class HistoricalClaimCreateDTO {
    
        private UUID submissionId; // Optional if created independently, though often linked
    
        @NotNull(message = "Subject Entity ID is required")
        private UUID subjectEntityId;
    
        @NotBlank(message = "Subject Entity Type is required")
        private String subjectEntityType;
    
        @NotBlank(message = "Predicate is required")
        private String predicate;
    
        @NotBlank(message = "Object Value is required")
        private String objectValue; // JSON payload
    
        private String normalizedValue;
    
        @NotNull(message = "Claim Type is required")
        private ClaimType claimType;
    
        private Double confidenceScore;
    }

    @Data
    public static class HistoricalClaimResponseDTO {
    
        private UUID claimId;
        private UUID submissionId;
        private UUID subjectEntityId;
        private String subjectEntityType;
        private String predicate;
        private String objectValue;
        private String normalizedValue;
        private ClaimType claimType;
        private ClaimStatus claimStatus;
        private Double confidenceScore;
        private UUID submittedById;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean canonicalFlag;
        private String moderationNotes;
    }

    @Data
    public static class HistoricalClaimUpdateDTO {
    
        private String predicate;
    
        private String objectValue;
    
        private String normalizedValue;
    
        private ClaimType claimType;
    
        private Double confidenceScore;
    }

    @Data
    public static class LoginRequestDTO {
        private String email;
        private String password;
    }

    @Data
    public static class RegistrationRequestDTO {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private Role role;
        private String adminCode;
        private UUID uniId;
    }

    @Data
    public static class ResearchSubmissionCreateDTO {
        
        @NotNull(message = "Civilization ID is required")
        private UUID civilizationId;
    
        @NotBlank(message = "Submission title is required")
        private String submissionTitle;
    
        private String submissionDescription;
    
        @NotBlank(message = "Payload is required")
        private String rawStructuredPayload;
    }

    @Data
    public static class ResearchSubmissionResponseDTO {
        private UUID submissionId;
        private UUID researcherId;
        private UUID institutionId;
        private UUID civilizationId;
        private String submissionTitle;
        private String submissionDescription;
        private String rawStructuredPayload;
        private ResearchSubmissionStatus submissionStatus;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime reviewedAt;
        private UUID reviewedById;
        private String remarks;
    }

    @Data
    public static class ResearchSubmissionUpdateDTO {
    
        private String submissionTitle;
    
        private String submissionDescription;
    
        private String rawStructuredPayload;
    }

    @Data
    public static class RollbackRequestDTO {
        private String hash;
    }

    @Data
    public static class CanonicalEntityCreateDTO {
        @NotBlank
        private String entityType;
    
        @NotBlank
        private String canonicalName;
    
        private String description;
        private Long startYear;
        private Long endYear;
        private UUID sourceEntityId;
        private Double confidenceScore;
        private List<String> aliases;
    }

    @Value
    @Builder
    public static class CanonicalEntityResponseDTO {
        UUID canonicalId;
        String entityType;
        String canonicalName;
        String normalizedName;
        String description;
        Long startYear;
        Long endYear;
        Double confidenceScore;
        CanonicalEntityStatus status;
        UUID mergedIntoId;
        List<String> aliases;
    }

    @Value
    @Builder
    public static class DuplicateCandidateResponseDTO {
        UUID candidateId;
        UUID leftCanonicalId;
        UUID rightCanonicalId;
        String leftName;
        String rightName;
        double similarityScore;
        String strategy;
        String explanation;
        DuplicateCandidateStatus status;
    }

    @Data
    public static class DuplicateDetectionRequest {
        private UUID canonicalId;
        private String entityType;
        private double threshold = 0.72;
        private boolean persistCandidates = true;
    }

    @Data
    public static class EntityAliasCreateDTO {
        @NotBlank
        private String aliasName;
    
        private AliasType aliasType = AliasType.ALIAS;
        private String languageCode;
        private String source;
        private Double confidenceScore;
    }

    @Value
    @Builder
    public static class EntityMatchDTO {
        UUID canonicalId;
        String entityType;
        String canonicalName;
        double similarityScore;
        String strategy;
        boolean aliasMatch;
        List<String> evidence;
    }

    @Data
    public static class EntityResolutionRequest {
        private String entityType;
        private String name;
        private Long startYear;
        private Long endYear;
        private double threshold = 0.72;
    }

    @Data
    public static class MergeEntitiesRequest {
        private UUID sourceCanonicalId;
        private UUID targetCanonicalId;
        private UUID mergedBy;
        private String reason;
        private boolean copyAliases = true;
    }

    @Data
    public static class ModerateDuplicateRequest {
        private DuplicateCandidateStatus status;
        private UUID reviewedBy;
        private String reviewNotes;
    }

    public static class GraphPathResultDTO {
        private UUID sourceId;
        private UUID targetId;
        private List<EdgeDTO> path;
        private Double pathConfidence; // Aggregated score along the edge traversal
    
        public static class EdgeDTO {
            public UUID id;
            public UUID from;
            public UUID to;
            public String relationshipType;
            public String temporalStart;
            public String temporalEnd;
            public Double confidenceScore;
            public String citations;
        }
    
        public UUID getSourceId() { return sourceId; }
        public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    
        public UUID getTargetId() { return targetId; }
        public void setTargetId(UUID targetId) { this.targetId = targetId; }
    
        public List<EdgeDTO> getPath() { return path; }
        public void setPath(List<EdgeDTO> path) { this.path = path; }
    
        public Double getPathConfidence() { return pathConfidence; }
        public void setPathConfidence(Double pathConfidence) { this.pathConfidence = pathConfidence; }
    }

    public static class ModerationActionRequestDTO {
        private String action; // APPROVE, REJECT, MERGE
        private String rationale;
        private Map<String, Object> metadata;
    
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
    
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
    
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    public static class ModerationTaskDetailDTO {
        private UUID taskId;
        private TaskStatus status;
        private ClaimSummary claim;
        private List<ConflictSummary> conflicts;
        private List<EntitySummary> entities;
    
        public static class ClaimSummary {
            public UUID id;
            public String statement;
            public List<String> provenance;
            public Double confidenceScore;
        }
    
        public static class ConflictSummary {
            public UUID conflictId;
            public String conflictingStatement;
            public String source;
            public Double score;
        }
    
        public static class EntitySummary {
            public UUID entityId;
            public String name;
            public List<String> aliases;
            public Double resolutionScore;
        }
    
        public UUID getTaskId() { return taskId; }
        public void setTaskId(UUID taskId) { this.taskId = taskId; }
    
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
    
        public ClaimSummary getClaim() { return claim; }
        public void setClaim(ClaimSummary claim) { this.claim = claim; }
    
        public List<ConflictSummary> getConflicts() { return conflicts; }
        public void setConflicts(List<ConflictSummary> conflicts) { this.conflicts = conflicts; }
    
        public List<EntitySummary> getEntities() { return entities; }
        public void setEntities(List<EntitySummary> entities) { this.entities = entities; }
    }

    public static class ModerationTaskListDTO {
        private UUID taskId;
        private TargetType targetType;
        private String snippet;
        private TaskStatus status;
        private UUID assignedTo;
        private TaskPriority priority;
        private LocalDateTime createdAt;
    
        public ModerationTaskListDTO(UUID taskId, TargetType targetType, String snippet, TaskStatus status, UUID assignedTo, TaskPriority priority, LocalDateTime createdAt) {
            this.taskId = taskId;
            this.targetType = targetType;
            this.snippet = snippet;
            this.status = status;
            this.assignedTo = assignedTo;
            this.priority = priority;
            this.createdAt = createdAt;
        }
    
        public UUID getTaskId() { return taskId; }
        public void setTaskId(UUID taskId) { this.taskId = taskId; }
    
        public TargetType getTargetType() { return targetType; }
        public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
    
        public TaskStatus getStatus() { return status; }
        public void setStatus(TaskStatus status) { this.status = status; }
    
        public UUID getAssignedTo() { return assignedTo; }
        public void setAssignedTo(UUID assignedTo) { this.assignedTo = assignedTo; }
    
        public TaskPriority getPriority() { return priority; }
        public void setPriority(TaskPriority priority) { this.priority = priority; }
    
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    @Data
    public static class HistoricalClaimInput {
        private String id;
        private String claimText;
        private String subjectEntityId;
        private String predicate;
        private String objectEntityId;
        private Long startYear;
        private Long endYear;
        private Double confidence;
        private Double sourceReliability;
        private Map<String, Object> attributes = new HashMap<>();
    }

    @Data
    public static class HistoricalEntityNode {
        private String id;
        private String type;
        private String name;
        private Long startYear;
        private Long endYear;
        private Long birthYear;
        private Long deathYear;
        private Double latitude;
        private Double longitude;
        private List<String> aliases;
        private Map<String, Object> attributes = new HashMap<>();
    }

    @Data
    public static class HistoricalRelationshipEdge {
        private String id;
        private String fromId;
        private String toId;
        private String type;
        private Long startYear;
        private Long endYear;
        private Double confidence;
        private Boolean exclusive;
        private Map<String, Object> attributes = new HashMap<>();
    }

    @Data
    public static class SemanticGraphValidationRequest {
        private List<HistoricalEntityNode> nodes = new ArrayList<>();
        private List<HistoricalRelationshipEdge> edges = new ArrayList<>();
        private List<HistoricalClaimInput> claims = new ArrayList<>();
        private boolean includeAiChecks = true;
        private boolean persistResults = false;
    }

    @Value
    @Builder
    public static class SemanticValidationFindingDTO {
        String ruleCode;
        String violationType;
        ValidationSeverity severity;
        double confidence;
        String entityId;
        String claimId;
        String message;
        List<String> evidence;
        Map<String, Object> metadata;
    }

    @Value
    @Builder
    public static class SemanticValidationResponse {
        int totalFindings;
        int criticalCount;
        int errorCount;
        int warningCount;
        int infoCount;
        boolean passed;
        List<SemanticValidationFindingDTO> findings;
    }

    /**
     * API response wrapping a full batch ValidationReport.
     */
    @Getter
    public static class ValidationReportResponseDTO {
    
        private final UUID batchId;
        private final LocalDateTime generatedAt;
        private final int totalSubmissions;
        private final long passedCount;
        private final long failedCount;
        private final long blockedCount;
        private final List<ValidationResultResponseDTO> results;
    
        public ValidationReportResponseDTO(ValidationReport report) {
            this.batchId = report.getBatchId();
            this.generatedAt = report.getGeneratedAt();
            this.totalSubmissions = report.getTotalSubmissions();
            this.passedCount = report.getPassedCount();
            this.failedCount = report.getFailedCount();
            this.blockedCount = report.getBlockedCount();
            this.results = report.getResults().stream()
                    .map(ValidationResultResponseDTO::new)
                    .toList();
        }
    }

    /**
     * API response wrapping a single ValidationResult.
     */
    @Getter
    public static class ValidationResultResponseDTO {
    
        private final UUID submissionId;
        private final boolean passed;
        private final boolean blocked;
        private final int totalErrors;
        private final List<ValidationError> criticalErrors;
        private final List<ValidationError> errors;
        private final List<ValidationError> warnings;
        private final List<ValidationError> infos;
    
        public ValidationResultResponseDTO(ValidationResult result) {
            this.submissionId = result.getSubmissionId();
            this.passed = result.isPassed();
            this.blocked = result.isBlocked();
            this.totalErrors = result.getErrorCount();
            this.criticalErrors = result.getErrorsBySeverity(ValidationSeverity.CRITICAL);
            this.errors       = result.getErrorsBySeverity(ValidationSeverity.ERROR);
            this.warnings     = result.getErrorsBySeverity(ValidationSeverity.WARNING);
            this.infos        = result.getErrorsBySeverity(ValidationSeverity.INFO);
        }
    }

}
