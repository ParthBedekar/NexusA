package com.example.nexusa.ConfidenceScoring.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "claim_confidence_scores")
@Data
public class ClaimConfidenceScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "score_id")
    private UUID scoreId;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "confidence_label", nullable = false)
    private String confidenceLabel;

    @Column(name = "scoring_version", nullable = false)
    private Integer scoringVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "factor_scores", columnDefinition = "jsonb", nullable = false)
    private String factorScores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explanation", columnDefinition = "jsonb", nullable = false)
    private String explanation;

    @Column(name = "calculated_by")
    private UUID calculatedBy;

    @Column(name = "superseded", nullable = false)
    private boolean superseded = false;

    @CreationTimestamp
    @Column(name = "calculated_at", updatable = false)
    private LocalDateTime calculatedAt;
}
