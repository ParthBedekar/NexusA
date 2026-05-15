CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS canonical_entities (
    canonical_id UUID PRIMARY KEY,
    entity_type VARCHAR(80) NOT NULL,
    canonical_name TEXT NOT NULL,
    normalized_name TEXT NOT NULL,
    description TEXT,
    start_year BIGINT,
    end_year BIGINT,
    source_entity_id UUID,
    confidence_score DOUBLE PRECISION DEFAULT 1.0,
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    merged_into_id UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS entity_aliases (
    alias_id UUID PRIMARY KEY,
    canonical_id UUID NOT NULL,
    alias_name TEXT NOT NULL,
    normalized_alias TEXT NOT NULL,
    alias_type VARCHAR(40) NOT NULL DEFAULT 'ALIAS',
    language_code VARCHAR(16),
    source TEXT,
    confidence_score DOUBLE PRECISION DEFAULT 1.0,
    created_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entity_alias_canonical FOREIGN KEY (canonical_id) REFERENCES canonical_entities(canonical_id)
);

CREATE TABLE IF NOT EXISTS duplicate_entity_candidates (
    candidate_id UUID PRIMARY KEY,
    left_canonical_id UUID NOT NULL,
    right_canonical_id UUID NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    strategy VARCHAR(80) NOT NULL,
    explanation TEXT,
    evidence JSONB,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID,
    review_notes TEXT,
    reviewed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_duplicate_left FOREIGN KEY (left_canonical_id) REFERENCES canonical_entities(canonical_id),
    CONSTRAINT fk_duplicate_right FOREIGN KEY (right_canonical_id) REFERENCES canonical_entities(canonical_id),
    CONSTRAINT chk_duplicate_order CHECK (left_canonical_id <> right_canonical_id)
);

CREATE TABLE IF NOT EXISTS entity_merge_audits (
    merge_id UUID PRIMARY KEY,
    source_canonical_id UUID NOT NULL,
    target_canonical_id UUID NOT NULL,
    merged_by UUID,
    reason TEXT,
    source_snapshot JSONB,
    target_snapshot JSONB,
    merged_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_canonical_entity_type_name
    ON canonical_entities(entity_type, normalized_name)
    WHERE status <> 'MERGED';

CREATE INDEX IF NOT EXISTS idx_canonical_status ON canonical_entities(status);
CREATE INDEX IF NOT EXISTS idx_canonical_source_entity ON canonical_entities(source_entity_id);
CREATE INDEX IF NOT EXISTS idx_alias_canonical ON entity_aliases(canonical_id);
CREATE INDEX IF NOT EXISTS idx_alias_normalized ON entity_aliases(normalized_alias);
CREATE INDEX IF NOT EXISTS idx_duplicate_status ON duplicate_entity_candidates(status);
CREATE INDEX IF NOT EXISTS idx_duplicate_score ON duplicate_entity_candidates(similarity_score DESC);

CREATE INDEX IF NOT EXISTS gin_canonical_name_trgm
    ON canonical_entities USING gin (normalized_name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS gin_alias_name_trgm
    ON entity_aliases USING gin (normalized_alias gin_trgm_ops);
