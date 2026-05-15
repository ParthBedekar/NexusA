package com.example.nexusa.SemanticValidation.model;
import com.example.nexusa.Model.Enums.GlobalEnums.SemanticFindingStatus;

import com.example.nexusa.Validation.core.GlobalValidationCore.ValidationSeverity;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "semantic_validation_results")
@Data
public class SemanticValidationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "result_id")
    private UUID resultId;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "violation_type", nullable = false)
    private String violationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private ValidationSeverity severity;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "claim_id")
    private String claimId;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence", columnDefinition = "jsonb")
    private String evidence;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SemanticFindingStatus status = SemanticFindingStatus.OPEN;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
