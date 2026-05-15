package com.example.nexusa.ConflictDetection.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "conflict_group_members")
@Data
public class ConflictGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_id")
    private UUID memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conflict_group_id", nullable = false)
    private ConflictGroup conflictGroup;

    @Column(name = "claim_id", nullable = false)
    private UUID claimId;

    @Column(name = "normalized_value", columnDefinition = "TEXT")
    private String normalizedValue;

    @Column(name = "claim_confidence")
    private Double claimConfidence;
}
