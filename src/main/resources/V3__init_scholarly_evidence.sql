-- ─── Citation Sources ─────────────────────────────────────────────────────────
-- Canonical registry of academic sources (books, journals, manuscripts, archives, etc.)
CREATE TABLE IF NOT EXISTS citation_sources (
    citation_id         UUID PRIMARY KEY,
    evidence_type       VARCHAR(50)       NOT NULL,
    title               VARCHAR(512)      NOT NULL,
    authors             TEXT,
    publication_year    INTEGER,
    publisher           VARCHAR(255),
    journal_name        VARCHAR(255),
    doi                 VARCHAR(255)      UNIQUE,
    isbn                VARCHAR(20)       UNIQUE,
    url                 TEXT,
    archive_name        VARCHAR(255),
    archive_location    VARCHAR(255),
    extended_metadata   JSONB,
    reliability_score   DOUBLE PRECISION  NOT NULL DEFAULT 0.5
                            CHECK (reliability_score >= 0.0 AND reliability_score <= 1.0),
    submitted_by        UUID              NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITHOUT TIME ZONE,
    version             BIGINT            NOT NULL DEFAULT 0,
    CONSTRAINT fk_citation_source_submitter FOREIGN KEY (submitted_by) REFERENCES users(user_id)
);

-- Indexing strategy: filter by type and score
CREATE INDEX idx_citation_sources_type         ON citation_sources(evidence_type);
CREATE INDEX idx_citation_sources_reliability  ON citation_sources(reliability_score DESC);
CREATE INDEX idx_citation_sources_year         ON citation_sources(publication_year);


-- ─── Claim Evidence ───────────────────────────────────────────────────────────
-- Attaches a CitationSource to a HistoricalClaim with quotation, page numbers,
-- extracted metadata, and uploaded document references.
CREATE TABLE IF NOT EXISTS claim_evidence (
    evidence_id              UUID PRIMARY KEY,
    claim_id                 UUID          NOT NULL,
    citation_id              UUID          NOT NULL,
    evidence_text            TEXT          NOT NULL,
    quoted_passage           TEXT,
    page_numbers             VARCHAR(100),
    extracted_metadata       JSONB,
    uploaded_documents       TEXT,
    source_reliability_score DOUBLE PRECISION
                                 CHECK (source_reliability_score >= 0.0 AND source_reliability_score <= 1.0),
    submitted_by             UUID          NOT NULL,
    created_at               TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version                  BIGINT        NOT NULL DEFAULT 0,
    CONSTRAINT fk_claim_evidence_claim    FOREIGN KEY (claim_id)     REFERENCES historical_claims(claim_id),
    CONSTRAINT fk_claim_evidence_citation FOREIGN KEY (citation_id)  REFERENCES citation_sources(citation_id),
    CONSTRAINT fk_claim_evidence_submitter FOREIGN KEY (submitted_by) REFERENCES users(user_id)
);

-- Indexing strategy: query evidence per claim, per citation source, filter by reliability
CREATE INDEX idx_claim_evidence_claim       ON claim_evidence(claim_id);
CREATE INDEX idx_claim_evidence_citation    ON claim_evidence(citation_id);
CREATE INDEX idx_claim_evidence_reliability ON claim_evidence(source_reliability_score DESC);
CREATE INDEX idx_claim_evidence_submitter   ON claim_evidence(submitted_by);
