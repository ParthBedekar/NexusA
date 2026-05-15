package com.example.nexusa.CanonicalKnowledge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provenance_records")
public class ProvenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "target_type", nullable = false)
    private String targetType; // ENTITY, FACT, RELATIONSHIP

    @Column(name = "original_claim_id")
    private UUID originalClaimId;

    @Column(name = "source_reference", nullable = false, columnDefinition = "TEXT")
    private String sourceReference;

    @Column(name = "validated_by_task_id")
    private UUID validatedByTaskId;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public UUID getOriginalClaimId() { return originalClaimId; }
    public void setOriginalClaimId(UUID originalClaimId) { this.originalClaimId = originalClaimId; }

    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }

    public UUID getValidatedByTaskId() { return validatedByTaskId; }
    public void setValidatedByTaskId(UUID validatedByTaskId) { this.validatedByTaskId = validatedByTaskId; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
