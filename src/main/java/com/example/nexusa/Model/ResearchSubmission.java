package com.example.nexusa.Model;

import com.example.nexusa.Model.Enums.GlobalEnums.ResearchSubmissionStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "research_submissions")
@Data
@SQLDelete(sql = "UPDATE research_submissions SET deleted = true WHERE submission_id = ? AND version = ?")
@SQLRestriction("deleted = false")
public class ResearchSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "submission_id")
    private UUID submissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "researcher_id", nullable = false)
    private User researcher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private University institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "civilization_id", nullable = false)
    private Civilization civilizationReference;

    @Column(name = "submission_title", nullable = false)
    private String submissionTitle;

    @Column(name = "submission_description", columnDefinition = "TEXT")
    private String submissionDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_structured_payload", columnDefinition = "jsonb")
    private String rawStructuredPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false)
    private ResearchSubmissionStatus submissionStatus = ResearchSubmissionStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
