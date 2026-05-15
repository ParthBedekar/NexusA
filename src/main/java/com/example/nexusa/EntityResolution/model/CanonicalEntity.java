package com.example.nexusa.EntityResolution.model;
import com.example.nexusa.Model.Enums.GlobalEnums.CanonicalEntityStatus;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "EntityResolutionCanonicalEntity")
@Table(name = "canonical_entities")
@Data
public class CanonicalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "canonical_id")
    private UUID canonicalId;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_year")
    private Long startYear;

    @Column(name = "end_year")
    private Long endYear;

    @Column(name = "source_entity_id")
    private UUID sourceEntityId;

    @Column(name = "confidence_score")
    private Double confidenceScore = 1.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CanonicalEntityStatus status = CanonicalEntityStatus.ACTIVE;

    @Column(name = "merged_into_id")
    private UUID mergedIntoId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
