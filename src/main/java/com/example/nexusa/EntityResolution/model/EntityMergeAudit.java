package com.example.nexusa.EntityResolution.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "EntityResolutionEntityMergeAudit")
@Table(name = "entity_merge_audits")
@Data
public class EntityMergeAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "merge_id")
    private UUID mergeId;

    @Column(name = "source_canonical_id", nullable = false)
    private UUID sourceCanonicalId;

    @Column(name = "target_canonical_id", nullable = false)
    private UUID targetCanonicalId;

    @Column(name = "merged_by")
    private UUID mergedBy;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_snapshot", columnDefinition = "jsonb")
    private String sourceSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_snapshot", columnDefinition = "jsonb")
    private String targetSnapshot;

    @CreationTimestamp
    @Column(name = "merged_at", updatable = false)
    private LocalDateTime mergedAt;
}
