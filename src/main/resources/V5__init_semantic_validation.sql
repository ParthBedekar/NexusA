CREATE TABLE IF NOT EXISTS semantic_validation_results (
    result_id UUID PRIMARY KEY,
    rule_code VARCHAR(128) NOT NULL,
    violation_type VARCHAR(128) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    entity_id VARCHAR(128),
    claim_id VARCHAR(128),
    message TEXT NOT NULL,
    evidence JSONB,
    metadata JSONB,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_semantic_validation_entity ON semantic_validation_results(entity_id);
CREATE INDEX idx_semantic_validation_claim ON semantic_validation_results(claim_id);
CREATE INDEX idx_semantic_validation_status ON semantic_validation_results(status);
CREATE INDEX idx_semantic_validation_violation ON semantic_validation_results(violation_type);
CREATE INDEX idx_semantic_validation_created ON semantic_validation_results(created_at DESC);
