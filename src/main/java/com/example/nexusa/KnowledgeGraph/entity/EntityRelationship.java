package com.example.nexusa.KnowledgeGraph.entity;
import com.example.nexusa.Model.Enums.GlobalEnums.RelationshipType;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.example.nexusa.CanonicalKnowledge.entity.CanonicalEntity;

@Entity
@Table(name = "entity_relationships")
public class EntityRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entity_id", nullable = false)
    private CanonicalEntity sourceEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_entity_id", nullable = false)
    private CanonicalEntity targetEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType;

    @Column(name = "temporal_start")
    private String temporalStart;

    @Column(name = "temporal_end")
    private String temporalEnd;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "citations", columnDefinition = "jsonb")
    private String citations; // Stored as a JSON array of provenance tracing citations

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public CanonicalEntity getSourceEntity() { return sourceEntity; }
    public void setSourceEntity(CanonicalEntity sourceEntity) { this.sourceEntity = sourceEntity; }

    public CanonicalEntity getTargetEntity() { return targetEntity; }
    public void setTargetEntity(CanonicalEntity targetEntity) { this.targetEntity = targetEntity; }

    public RelationshipType getRelationshipType() { return relationshipType; }
    public void setRelationshipType(RelationshipType relationshipType) { this.relationshipType = relationshipType; }

    public String getTemporalStart() { return temporalStart; }
    public void setTemporalStart(String temporalStart) { this.temporalStart = temporalStart; }

    public String getTemporalEnd() { return temporalEnd; }
    public void setTemporalEnd(String temporalEnd) { this.temporalEnd = temporalEnd; }

    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getCitations() { return citations; }
    public void setCitations(String citations) { this.citations = citations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
