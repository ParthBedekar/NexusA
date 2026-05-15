package com.example.nexusa.Model;

import com.example.nexusa.Model.Enums.GlobalEnums.EvidenceType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an academic or archival source that can be attached to a claim as evidence.
 * Supports books, journals, manuscripts, museum archives, DOI, ISBN, and URLs.
 */
@Entity
@Table(name = "citation_sources")
@Data
public class CitationSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "citation_id")
    private UUID citationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false)
    private EvidenceType evidenceType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "journal_name")
    private String journalName;

    @Column(name = "doi")
    private String doi;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "archive_name")
    private String archiveName;

    @Column(name = "archive_location")
    private String archiveLocation;

    // JSONB — flexible metadata: volume, issue, edition, accession number, etc.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extended_metadata", columnDefinition = "jsonb")
    private String extendedMetadata;

    // Reliability score between 0.0 and 1.0, assigned on creation and updated after review
    @Column(name = "reliability_score", nullable = false)
    private Double reliabilityScore = 0.5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

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
