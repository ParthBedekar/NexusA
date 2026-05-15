CREATE TABLE IF NOT EXISTS scoring_weight_configs (
    config_id UUID PRIMARY KEY,
    version INTEGER NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    weights JSONB NOT NULL,
    status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE',
    created_by UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS claim_confidence_scores (
    score_id UUID PRIMARY KEY,
    claim_id UUID NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    confidence_label VARCHAR(40) NOT NULL,
    scoring_version INTEGER NOT NULL,
    factor_scores JSONB NOT NULL,
    explanation JSONB NOT NULL,
    calculated_by UUID,
    superseded BOOLEAN NOT NULL DEFAULT FALSE,
    calculated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_confidence_score_claim FOREIGN KEY (claim_id) REFERENCES historical_claims(claim_id)
);

CREATE INDEX IF NOT EXISTS idx_confidence_scores_claim_time
    ON claim_confidence_scores(claim_id, calculated_at DESC);

CREATE INDEX IF NOT EXISTS idx_confidence_scores_latest
    ON claim_confidence_scores(claim_id)
    WHERE superseded = FALSE;

CREATE INDEX IF NOT EXISTS idx_confidence_scores_score
    ON claim_confidence_scores(score DESC);

CREATE INDEX IF NOT EXISTS idx_scoring_config_status
    ON scoring_weight_configs(status, version DESC);

INSERT INTO scoring_weight_configs (config_id, version, name, weights, status)
SELECT '00000000-0000-0000-0000-000000000801'::uuid, 1, 'Default NexusA Confidence Model',
       '{
          "institutionReputation": 0.12,
          "citationQuality": 0.14,
          "researcherReputation": 0.10,
          "consensusSimilarity": 0.14,
          "sourceReliability": 0.16,
          "moderatorApproval": 0.12,
          "historicalConsistency": 0.12,
          "evidenceQuality": 0.10
        }'::jsonb,
       'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM scoring_weight_configs WHERE version = 1);
