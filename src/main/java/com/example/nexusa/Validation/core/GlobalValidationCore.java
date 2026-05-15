package com.example.nexusa.Validation.core;

import com.example.nexusa.Model.ResearchSubmission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.springframework.stereotype.Component;

public class GlobalValidationCore {

    /**
     * Carries all inputs needed by validation rules during a pipeline run.
     * Passed through every rule without mutation — rules only read from it.
     * Extra rule-specific data can be placed in the extensionData map.
     */
    @Getter
    @Builder
    public static class ValidationContext {
    
        /** The submission being validated. */
        private final ResearchSubmission submission;
    
        /**
         * Parsed representation of the raw JSONB payload (as a Map).
         * Null if the payload could not be parsed — rules should handle this gracefully.
         */
        private final Map<String, Object> parsedPayload;
    
        /**
         * Open-ended extension data for rules that need extra domain context
         * (e.g., known civilization IDs, known entity types).
         */
        @Builder.Default
        private final Map<String, Object> extensionData = new HashMap<>();
    
        public Object getExtension(String key) {
            return extensionData.get(key);
        }
    }

    /**
     * An immutable value object representing a single validation failure.
     * Created by a ValidationRule and collected into a ValidationResult.
     */
    @Value
    @Builder
    public static class ValidationError {
    
        /** The rule that produced this error (e.g., "REQUIRED_FIELDS", "CHRONOLOGY"). */
        String ruleCode;
    
        /** The field or JSON path that failed validation (e.g., "submissionTitle", "payload.startDate"). */
        String fieldPath;
    
        /** Human-readable description of what went wrong. */
        String message;
    
        /** Severity of this error — drives blocking vs advisory logic downstream. */
        ValidationSeverity severity;
    
        /** Optional: the actual value that was found (useful for audit logging). */
        String rejectedValue;
    }

    /**
     * Orchestrates validation rule execution in order.
     *
     * Pipeline behaviour:
     * - Runs all registered rules sequentially.
     * - If a short-circuit rule produces CRITICAL errors, the pipeline stops early.
     * - Collects all errors from all rules into a single ValidationResult.
     */
    @Component
    public static class ValidationPipeline {
    
        private final ValidationRuleRegistry registry;
    
        public ValidationPipeline(ValidationRuleRegistry registry) {
            this.registry = registry;
        }
    
        /**
         * Validates a single submission context through the full rule pipeline.
         */
        public ValidationResult run(ValidationContext context) {
            ValidationResult result = new ValidationResult(
                    context.getSubmission().getSubmissionId()
            );
    
            for (ValidationRule rule : registry.getAllRules()) {
                List<ValidationError> errors = rule.validate(context);
                result.addAll(errors);
    
                // Short-circuit: if the rule requests it and CRITICAL errors were found, stop
                if (rule.isShortCircuit() && result.isBlocked()) {
                    break;
                }
            }
    
            return result;
        }
    
        /**
         * Validates a batch of contexts and aggregates results into a ValidationReport.
         */
        public ValidationReport runBatch(List<ValidationContext> contexts) {
            ValidationReport report = new ValidationReport();
            for (ValidationContext context : contexts) {
                report.addResult(run(context));
            }
            return report;
        }
    }

    /**
     * Aggregates results from a batch validation run across multiple submissions.
     */
    @Getter
    public static class ValidationReport {
    
        private final UUID batchId = UUID.randomUUID();
        private final LocalDateTime generatedAt = LocalDateTime.now();
        private final List<ValidationResult> results = new ArrayList<>();
    
        public void addResult(ValidationResult result) {
            results.add(result);
        }
    
        public int getTotalSubmissions() {
            return results.size();
        }
    
        public long getPassedCount() {
            return results.stream().filter(ValidationResult::isPassed).count();
        }
    
        public long getFailedCount() {
            return results.stream().filter(r -> !r.isPassed()).count();
        }
    
        public long getBlockedCount() {
            return results.stream().filter(ValidationResult::isBlocked).count();
        }
    
        public List<ValidationResult> getResults() {
            return Collections.unmodifiableList(results);
        }
    
        public List<ValidationResult> getFailedResults() {
            return results.stream().filter(r -> !r.isPassed()).toList();
        }
    }

    /**
     * Accumulates all ValidationErrors for a single submission validation run.
     * Determines whether the submission passes, has warnings only, or is blocked.
     */
    @Getter
    public static class ValidationResult {
    
        private final UUID submissionId;
        private final List<ValidationError> errors = new ArrayList<>();
    
        public ValidationResult(UUID submissionId) {
            this.submissionId = submissionId;
        }
    
        public void addError(ValidationError error) {
            errors.add(error);
        }
    
        public void addAll(List<ValidationError> newErrors) {
            errors.addAll(newErrors);
        }
    
        /** True if there are no CRITICAL or ERROR-severity failures. */
        public boolean isPassed() {
            return errors.stream().noneMatch(e ->
                    e.getSeverity() == ValidationSeverity.CRITICAL ||
                    e.getSeverity() == ValidationSeverity.ERROR
            );
        }
    
        /** True if any CRITICAL-severity failure exists — submission must be blocked immediately. */
        public boolean isBlocked() {
            return errors.stream().anyMatch(e -> e.getSeverity() == ValidationSeverity.CRITICAL);
        }
    
        public List<ValidationError> getErrorsBySeverity(ValidationSeverity severity) {
            return errors.stream().filter(e -> e.getSeverity() == severity).toList();
        }
    
        public List<ValidationError> getErrors() {
            return Collections.unmodifiableList(errors);
        }
    
        public int getErrorCount() {
            return errors.size();
        }
    }

    /**
     * Core interface for all validation rules in the Structural Validation Engine.
     *
     * To add a new rule:
     *   1. Create a class implementing ValidationRule.
     *   2. Annotate it with @Component (Spring will auto-register it).
     *   3. The ValidationRuleRegistry picks it up automatically.
     *
     * Rules are stateless — all input comes via ValidationContext.
     */
    public interface ValidationRule {
    
        /**
         * A short, unique, machine-readable code identifying this rule.
         * Used in ValidationError and audit logs. E.g. "REQUIRED_FIELDS", "CHRONOLOGY".
         */
        String getRuleCode();
    
        /**
         * Human-readable description of what this rule checks.
         * Appears in API responses and dashboards.
         */
        String getDescription();
    
        /**
         * Executes the rule against the given context.
         *
         * @param context the validation context containing the submission and parsed payload
         * @return a list of ValidationError objects. Empty list means this rule passed.
         */
        List<ValidationError> validate(ValidationContext context);
    
        /**
         * Returns true if this rule should abort the entire pipeline
         * when it finds CRITICAL errors (short-circuit execution).
         * Default: false — pipeline continues regardless.
         */
        default boolean isShortCircuit() {
            return false;
        }
    }

    /**
     * Auto-discovers all ValidationRule beans and makes them accessible by ruleCode.
     * Spring injects all @Component-annotated ValidationRule implementations automatically.
     */
    @Component
    public static class ValidationRuleRegistry {
    
        private final Map<String, ValidationRule> rulesByCode;
        private final List<ValidationRule> allRules;
    
        public ValidationRuleRegistry(List<ValidationRule> rules) {
            this.allRules = Collections.unmodifiableList(rules);
            this.rulesByCode = rules.stream()
                    .collect(Collectors.toMap(ValidationRule::getRuleCode, Function.identity()));
        }
    
        public List<ValidationRule> getAllRules() {
            return allRules;
        }
    
        public ValidationRule getRule(String ruleCode) {
            ValidationRule rule = rulesByCode.get(ruleCode);
            if (rule == null) {
                throw new IllegalArgumentException("No validation rule registered with code: " + ruleCode);
            }
            return rule;
        }
    
        public boolean hasRule(String ruleCode) {
            return rulesByCode.containsKey(ruleCode);
        }
    }

    /**
     * Severity levels for validation errors in the Structural Validation Engine.
     *
     * CRITICAL - Submission cannot proceed. Hard stop (e.g., missing required fields, invalid civilization reference).
     * ERROR     - Serious structural problem that must be fixed (e.g., invalid date range, bad chronology).
     * WARNING   - Non-blocking issue flagged for moderator review (e.g., low confidence score, unusual metadata).
     * INFO      - Informational hint for the researcher (e.g., suggested normalization, style guide notes).
     */
    public static enum ValidationSeverity {
        CRITICAL,
        ERROR,
        WARNING,
        INFO
    }

}
