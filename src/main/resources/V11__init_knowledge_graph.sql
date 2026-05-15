CREATE TABLE entity_relationships (
    id UUID PRIMARY KEY,
    source_entity_id UUID NOT NULL REFERENCES canonical_entities(id) ON DELETE CASCADE,
    target_entity_id UUID NOT NULL REFERENCES canonical_entities(id) ON DELETE CASCADE,
    relationship_type VARCHAR(50) NOT NULL,
    temporal_start VARCHAR(100),
    temporal_end VARCHAR(100),
    confidence_score DOUBLE PRECISION NOT NULL,
    citations JSONB, -- Stores the citations tracking the source provenance
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(source_entity_id, target_entity_id, relationship_type)
);

-- Indexing Strategy for Graph Traversals
CREATE INDEX idx_kg_source ON entity_relationships (source_entity_id);
CREATE INDEX idx_kg_target ON entity_relationships (target_entity_id);
CREATE INDEX idx_kg_type ON entity_relationships (relationship_type);
CREATE INDEX idx_kg_bidirectional ON entity_relationships (source_entity_id, target_entity_id);

-- GIN Index for parsing Citation JSONB objects (support citation-awareness searches)
CREATE INDEX idx_kg_citations ON entity_relationships USING GIN (citations);
