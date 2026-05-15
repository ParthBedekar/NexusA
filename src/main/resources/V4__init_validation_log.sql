CREATE TABLE IF NOT EXISTS validation_logs (
    log_id          UUID PRIMARY KEY,
    submission_id   UUID NOT NULL,
    triggered_by    UUID NOT NULL,
    passed          BOOLEAN NOT NULL DEFAULT FALSE,
    blocked         BOOLEAN NOT NULL DEFAULT FALSE,
    total_errors    INTEGER NOT NULL DEFAULT 0,
    critical_count  INTEGER NOT NULL DEFAULT 0,
    error_count     INTEGER NOT NULL DEFAULT 0,
    warning_count   INTEGER NOT NULL DEFAULT 0,
    info_count      INTEGER NOT NULL DEFAULT 0,
    errors_payload  JSONB,
    validated_at    TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_validation_log_triggered_by FOREIGN KEY (triggered_by) REFERENCES users(user_id)
);

CREATE INDEX idx_validation_logs_submission  ON validation_logs(submission_id);
CREATE INDEX idx_validation_logs_blocked     ON validation_logs(blocked) WHERE blocked = TRUE;
CREATE INDEX idx_validation_logs_passed      ON validation_logs(passed);
CREATE INDEX idx_validation_logs_trigger     ON validation_logs(triggered_by);
CREATE INDEX idx_validation_logs_timestamp   ON validation_logs(validated_at DESC);
