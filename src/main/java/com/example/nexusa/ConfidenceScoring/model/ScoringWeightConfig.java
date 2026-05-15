package com.example.nexusa.ConfidenceScoring.model;
import com.example.nexusa.Model.Enums.GlobalEnums.ScoringConfigStatus;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scoring_weight_configs")
@Data
public class ScoringWeightConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "config_id")
    private UUID configId;

    @Column(name = "version", nullable = false, unique = true)
    private Integer version;

    @Column(name = "name", nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weights", columnDefinition = "jsonb", nullable = false)
    private String weights;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScoringConfigStatus status = ScoringConfigStatus.ACTIVE;

    @Column(name = "created_by")
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
