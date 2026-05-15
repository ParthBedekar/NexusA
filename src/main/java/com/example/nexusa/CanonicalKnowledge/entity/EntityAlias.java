package com.example.nexusa.CanonicalKnowledge.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "entity_aliases")
public class EntityAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canonical_entity_id", nullable = false)
    private CanonicalEntity canonicalEntity;

    @Column(name = "alias_name", nullable = false)
    private String aliasName;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public CanonicalEntity getCanonicalEntity() { return canonicalEntity; }
    public void setCanonicalEntity(CanonicalEntity canonicalEntity) { this.canonicalEntity = canonicalEntity; }

    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }
}
