package com.example.nexusa.EntityResolution.model;
import com.example.nexusa.Model.Enums.GlobalEnums.DuplicateCandidateStatus;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "EntityResolutionDuplicateCandidate")
@Table(name = "duplicate_entity_candidates")
@Data
public class DuplicateCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "candidate_id")
    private UUID candidateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "left_canonical_id", nullable = false)
    private CanonicalEntity leftEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "right_canonical_id", nullable = false)
    private CanonicalEntity rightEntity;

    @Column(name = "similarity_score", nullable = false)
    private Double similarityScore;

    @Column(name = "strategy", nullable = false)
    private String strategy;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "evidence", columnDefinition = "jsonb")
    private String evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DuplicateCandidateStatus status = DuplicateCandidateStatus.PENDING;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "review_notes", columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
