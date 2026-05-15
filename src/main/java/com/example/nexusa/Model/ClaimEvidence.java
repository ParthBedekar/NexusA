package com.example.nexusa.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a piece of scholarly evidence attached to a specific HistoricalClaim.
 * Each evidence record links a claim to a CitationSource, along with quotations,
 * page numbers, extracted metadata, and uploaded document references.
 */
@Entity
@Table(name = "claim_evidence")
@Data
public class ClaimEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "evidence_id")
    private UUID evidenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private HistoricalClaim claim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citation_id", nullable = false)
    private CitationSource citationSource;

    @Column(name = "evidence_text", columnDefinition = "TEXT", nullable = false)
    private String evidenceText;

    @Column(name = "quoted_passage", columnDefinition = "TEXT")
    private String quotedPassage;

    @Column(name = "page_numbers")
    private String pageNumbers;

    // JSONB — stores structured extracted metadata: volume, edition, chapter, coordinates, etc.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_metadata", columnDefinition = "jsonb")
    private String extractedMetadata;

    // Comma-separated or JSON array of file paths / storage URLs for uploaded documents
    @Column(name = "uploaded_documents", columnDefinition = "TEXT")
    private String uploadedDocuments;

    // Inherited from CitationSource but can be overridden per evidence usage context
    @Column(name = "source_reliability_score")
    private Double sourceReliabilityScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version")
    private Long version;
}
