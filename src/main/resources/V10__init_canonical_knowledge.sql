CREATE TABLE canonical_entities (
    id UUID PRIMARY KEY,
    canonical_name VARCHAR(255) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    description TEXT,
    temporal_start VARCHAR(100),
    temporal_end VARCHAR(100),
    current_version INT DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE entity_aliases (
    id UUID PRIMARY KEY,
    canonical_entity_id UUID NOT NULL REFERENCES canonical_entities(id) ON DELETE CASCADE,
    alias_name VARCHAR(255) NOT NULL,
    UNIQUE(canonical_entity_id, alias_name)
);

CREATE TABLE canonical_facts (
    id UUID PRIMARY KEY,
    subject_entity_id UUID NOT NULL REFERENCES canonical_entities(id) ON DELETE CASCADE,
    statement TEXT NOT NULL,
    temporal_start VARCHAR(100),
    temporal_end VARCHAR(100),
    confidence_score DOUBLE PRECISION NOT NULL,
    current_version INT DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE canonical_relationships (
    id UUID PRIMARY KEY,
    source_entity_id UUID NOT NULL REFERENCES canonical_entities(id) ON DELETE CASCADE,
    target_entity_id UUID NOT NULL REFERENCES canonical_entities(id) ON DELETE CASCADE,
    relationship_type VARCHAR(100) NOT NULL,
    description TEXT,
    temporal_start VARCHAR(100),
    temporal_end VARCHAR(100),
    confidence_weight DOUBLE PRECISION DEFAULT 1.0,
    UNIQUE(source_entity_id, target_entity_id, relationship_type)
);

CREATE TABLE provenance_records (
    id UUID PRIMARY KEY,
    target_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL, -- 'ENTITY', 'FACT', 'RELATIONSHIP'
    original_claim_id UUID,
    source_reference TEXT NOT NULL,
    validated_by_task_id UUID,
    added_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE canonical_versions (
    id UUID PRIMARY KEY,
    target_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    version_number INT NOT NULL,
    state_payload JSONB NOT NULL,
    change_reason TEXT,
    changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(target_id, target_type, version_number)
);

-- Indexing Strategy for Read Optimization & AI Retrieval Support
-- 1. B-Tree indexes for fast exact/FK lookups
CREATE INDEX idx_canonical_entities_type ON canonical_entities(entity_type);
CREATE INDEX idx_entity_aliases_entity_id ON entity_aliases(canonical_entity_id);
CREATE INDEX idx_canonical_facts_subject_id ON canonical_facts(subject_entity_id);
CREATE INDEX idx_canonical_rels_source ON canonical_relationships(source_entity_id);
CREATE INDEX idx_canonical_rels_target ON canonical_relationships(target_entity_id);
CREATE INDEX idx_provenance_target ON provenance_records(target_id, target_type);

-- 2. GIN Indexes for Full-Text Search (critical for AI retrieval / embedding mapping context)
ALTER TABLE canonical_entities ADD COLUMN searchable_text tsvector 
    GENERATED ALWAYS AS (to_tsvector('english', coalesce(canonical_name, '') || ' ' || coalesce(description, ''))) STORED;
CREATE INDEX idx_fts_canonical_entities ON canonical_entities USING GIN (searchable_text);

ALTER TABLE canonical_facts ADD COLUMN searchable_text tsvector 
    GENERATED ALWAYS AS (to_tsvector('english', coalesce(statement, ''))) STORED;
CREATE INDEX idx_fts_canonical_facts ON canonical_facts USING GIN (searchable_text);
