CREATE TABLE IF NOT EXISTS research_submissions (
    submission_id UUID PRIMARY KEY,
    researcher_id UUID NOT NULL,
    institution_id UUID NOT NULL,
    civilization_id UUID NOT NULL,
    submission_title VARCHAR(255) NOT NULL,
    submission_description TEXT,
    raw_structured_payload JSONB NOT NULL,
    submission_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    reviewed_at TIMESTAMP WITHOUT TIME ZONE,
    reviewed_by UUID,
    remarks TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_research_submission_researcher FOREIGN KEY (researcher_id) REFERENCES users(user_id),
    CONSTRAINT fk_research_submission_institution FOREIGN KEY (institution_id) REFERENCES universities(uni_id),
    CONSTRAINT fk_research_submission_civilization FOREIGN KEY (civilization_id) REFERENCES civilizations(civ_id),
    CONSTRAINT fk_research_submission_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
);

CREATE INDEX idx_research_submissions_status ON research_submissions(submission_status);
CREATE INDEX idx_research_submissions_civilization ON research_submissions(civilization_id);
CREATE INDEX idx_research_submissions_institution ON research_submissions(institution_id);
