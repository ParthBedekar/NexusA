package com.example.nexusa.CanonicalKnowledge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "canonical_entities")
public class CanonicalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "canonical_name", nullable = false)
    private String canonicalName;

    @Column(name = "entity_type", nullable = false)
    private String entityType; // e.g. PERSON, CIVILIZATION, ARTIFACT, EVENT

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "temporal_start")
    private String temporalStart;

    @Column(name = "temporal_end")
    private String temporalEnd;

    @Column(name = "current_version")
    private Integer currentVersion = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "canonicalEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EntityAlias> aliases = new HashSet<>();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTemporalStart() { return temporalStart; }
    public void setTemporalStart(String temporalStart) { this.temporalStart = temporalStart; }

    public String getTemporalEnd() { return temporalEnd; }
    public void setTemporalEnd(String temporalEnd) { this.temporalEnd = temporalEnd; }

    public Integer getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(Integer currentVersion) { this.currentVersion = currentVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<EntityAlias> getAliases() { return aliases; }
    public void setAliases(Set<EntityAlias> aliases) { this.aliases = aliases; }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
