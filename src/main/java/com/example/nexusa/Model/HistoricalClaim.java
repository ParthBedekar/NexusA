package com.example.nexusa.Model;

import com.example.nexusa.Model.Enums.GlobalEnums.ClaimStatus;
import com.example.nexusa.Model.Enums.GlobalEnums.ClaimType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historical_claims")
@Data
public class HistoricalClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "claim_id")
    private UUID claimId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private ResearchSubmission submission;

    @Column(name = "subject_entity_id", nullable = false)
    private UUID subjectEntityId;

    @Column(name = "subject_entity_type", nullable = false)
    private String subjectEntityType;

    @Column(name = "predicate", nullable = false)
    private String predicate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "object_value", columnDefinition = "jsonb", nullable = false)
    private String objectValue;

    @Column(name = "normalized_value", columnDefinition = "TEXT")
    private String normalizedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_type", nullable = false)
    private ClaimType claimType;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", nullable = false)
    private ClaimStatus claimStatus = ClaimStatus.PENDING;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "canonical_flag", nullable = false)
    private boolean canonicalFlag = false;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    @Version
    @Column(name = "version")
    private Long version;
}
