package com.example.nexusa.CanonicalKnowledge.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "canonical_relationships")
public class CanonicalRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entity_id", nullable = false)
    private CanonicalEntity sourceEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_entity_id", nullable = false)
    private CanonicalEntity targetEntity;

    @Column(name = "relationship_type", nullable = false)
    private String relationshipType; // e.g. LOCATED_IN, FOUNDED_BY, ENEMY_OF

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "temporal_start")
    private String temporalStart;

    @Column(name = "temporal_end")
    private String temporalEnd;

    @Column(name = "confidence_weight")
    private Double confidenceWeight = 1.0;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CanonicalEntity getSourceEntity() { return sourceEntity; }
    public void setSourceEntity(CanonicalEntity sourceEntity) { this.sourceEntity = sourceEntity; }

    public CanonicalEntity getTargetEntity() { return targetEntity; }
    public void setTargetEntity(CanonicalEntity targetEntity) { this.targetEntity = targetEntity; }

    public String getRelationshipType() { return relationshipType; }
    public void setRelationshipType(String relationshipType) { this.relationshipType = relationshipType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTemporalStart() { return temporalStart; }
    public void setTemporalStart(String temporalStart) { this.temporalStart = temporalStart; }

    public String getTemporalEnd() { return temporalEnd; }
    public void setTemporalEnd(String temporalEnd) { this.temporalEnd = temporalEnd; }

    public Double getConfidenceWeight() { return confidenceWeight; }
    public void setConfidenceWeight(Double confidenceWeight) { this.confidenceWeight = confidenceWeight; }
}
