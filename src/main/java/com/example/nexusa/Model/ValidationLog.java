package com.example.nexusa.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persists the outcome of every validation pipeline run for a submission.
 * Provides a full audit trail for moderators and researchers.
 */
@Entity
@Table(name = "validation_logs")
@Data
public class ValidationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id")
    private UUID logId;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Column(name = "triggered_by", nullable = false)
    private UUID triggeredBy; // User ID of the moderator or system that triggered validation

    @Column(name = "passed", nullable = false)
    private boolean passed;

    @Column(name = "blocked", nullable = false)
    private boolean blocked;

    @Column(name = "total_errors", nullable = false)
    private int totalErrors;

    @Column(name = "critical_count", nullable = false)
    private int criticalCount;

    @Column(name = "error_count", nullable = false)
    private int errorCount;

    @Column(name = "warning_count", nullable = false)
    private int warningCount;

    @Column(name = "info_count", nullable = false)
    private int infoCount;

    // Full JSON dump of all ValidationError objects for this run
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "errors_payload", columnDefinition = "jsonb")
    private String errorsPayload;

    @CreationTimestamp
    @Column(name = "validated_at", updatable = false)
    private LocalDateTime validatedAt;
}
