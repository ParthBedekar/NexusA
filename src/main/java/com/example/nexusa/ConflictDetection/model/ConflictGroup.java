package com.example.nexusa.ConflictDetection.model;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictType;
import com.example.nexusa.Model.Enums.GlobalEnums.ConflictGroupStatus;

import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "conflict_groups")
@Data
public class ConflictGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conflict_group_id")
    private UUID conflictGroupId;

    @Column(name = "subject_entity_id", nullable = false)
    private UUID subjectEntityId;

    @Column(name = "subject_entity_type", nullable = false)
    private String subjectEntityType;

    @Column(name = "predicate", nullable = false)
    private String predicate;

    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_type", nullable = false)
    private ConflictType conflictType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private ValidationSeverity severity;

    @Column(name = "severity_score", nullable = false)
    private Double severityScore;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conflicting_values", columnDefinition = "jsonb")
    private String conflictingValues;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence", columnDefinition = "jsonb")
    private String evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ConflictGroupStatus status = ConflictGroupStatus.OPEN;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "resolved_claim_id")
    private UUID resolvedClaimId;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
