CREATE TABLE IF NOT EXISTS conflict_groups (
    conflict_group_id UUID PRIMARY KEY,
    subject_entity_id UUID NOT NULL,
    subject_entity_type VARCHAR(255) NOT NULL,
    predicate VARCHAR(255) NOT NULL,
    conflict_type VARCHAR(50) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    severity_score DOUBLE PRECISION NOT NULL,
    confidence_score DOUBLE PRECISION NOT NULL,
    summary TEXT NOT NULL,
    conflicting_values JSONB,
    evidence JSONB,
    status VARCHAR(40) NOT NULL DEFAULT 'OPEN',
    reviewed_by UUID,
    review_notes TEXT,
    resolved_claim_id UUID,
    resolved_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE IF NOT EXISTS conflict_group_members (
    member_id UUID PRIMARY KEY,
    conflict_group_id UUID NOT NULL,
    claim_id UUID NOT NULL,
    normalized_value TEXT,
    claim_confidence DOUBLE PRECISION,
    CONSTRAINT fk_conflict_member_group FOREIGN KEY (conflict_group_id) REFERENCES conflict_groups(conflict_group_id),
    CONSTRAINT fk_conflict_member_claim FOREIGN KEY (claim_id) REFERENCES historical_claims(claim_id)
);

CREATE INDEX IF NOT EXISTS idx_conflict_group_subject_predicate
    ON conflict_groups(subject_entity_id, predicate);

CREATE INDEX IF NOT EXISTS idx_conflict_group_status_severity
    ON conflict_groups(status, severity_score DESC);

CREATE INDEX IF NOT EXISTS idx_conflict_group_type
    ON conflict_groups(conflict_type);

CREATE INDEX IF NOT EXISTS idx_conflict_member_group
    ON conflict_group_members(conflict_group_id);

CREATE INDEX IF NOT EXISTS idx_conflict_member_claim
    ON conflict_group_members(claim_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_open_conflict_group_subject_predicate_type
    ON conflict_groups(subject_entity_id, predicate, conflict_type)
    WHERE status IN ('OPEN', 'UNDER_REVIEW');
