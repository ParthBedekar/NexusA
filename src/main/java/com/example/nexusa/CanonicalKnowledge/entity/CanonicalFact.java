package com.example.nexusa.CanonicalKnowledge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "canonical_facts")
public class CanonicalFact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_entity_id", nullable = false)
    private CanonicalEntity subjectEntity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String statement;

    @Column(name = "temporal_start")
    private String temporalStart;

    @Column(name = "temporal_end")
    private String temporalEnd;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(name = "current_version")
    private Integer currentVersion = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CanonicalEntity getSubjectEntity() { return subjectEntity; }
    public void setSubjectEntity(CanonicalEntity subjectEntity) { this.subjectEntity = subjectEntity; }

    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }

    public String getTemporalStart() { return temporalStart; }
    public void setTemporalStart(String temporalStart) { this.temporalStart = temporalStart; }

    public String getTemporalEnd() { return temporalEnd; }
    public void setTemporalEnd(String temporalEnd) { this.temporalEnd = temporalEnd; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public Integer getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(Integer currentVersion) { this.currentVersion = currentVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
