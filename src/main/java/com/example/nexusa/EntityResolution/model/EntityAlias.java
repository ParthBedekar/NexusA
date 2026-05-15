package com.example.nexusa.EntityResolution.model;
import com.example.nexusa.Model.Enums.GlobalEnums.AliasType;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "EntityResolutionEntityAlias")
@Table(name = "entity_aliases")
@Data
public class EntityAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alias_id")
    private UUID aliasId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_id", nullable = false)
    private CanonicalEntity canonicalEntity;

    @Column(name = "alias_name", nullable = false)
    private String aliasName;

    @Column(name = "normalized_alias", nullable = false)
    private String normalizedAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "alias_type", nullable = false)
    private AliasType aliasType = AliasType.ALIAS;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "source")
    private String source;

    @Column(name = "confidence_score")
    private Double confidenceScore = 1.0;

    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
