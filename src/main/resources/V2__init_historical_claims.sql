CREATE TABLE IF NOT EXISTS historical_claims (
    claim_id UUID PRIMARY KEY,
    submission_id UUID,
    subject_entity_id UUID NOT NULL,
    subject_entity_type VARCHAR(255) NOT NULL,
    predicate VARCHAR(255) NOT NULL,
    object_value JSONB NOT NULL,
    normalized_value TEXT,
    claim_type VARCHAR(50) NOT NULL,
    claim_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    confidence_score DOUBLE PRECISION,
    submitted_by UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    canonical_flag BOOLEAN NOT NULL DEFAULT FALSE,
    moderation_notes TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_historical_claim_submission FOREIGN KEY (submission_id) REFERENCES research_submissions(submission_id),
    CONSTRAINT fk_historical_claim_submitter FOREIGN KEY (submitted_by) REFERENCES users(user_id)
);

CREATE INDEX idx_historical_claims_subject_predicate ON historical_claims(subject_entity_id, predicate);
CREATE INDEX idx_historical_claims_submission ON historical_claims(submission_id);
CREATE INDEX idx_historical_claims_status ON historical_claims(claim_status);
CREATE INDEX idx_historical_claims_canonical ON historical_claims(canonical_flag);
