CREATE TABLE moderation_tasks (
    id UUID PRIMARY KEY,
    target_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assignee_id UUID,
    priority VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE moderation_comments (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES moderation_tasks(id) ON DELETE CASCADE,
    author_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE moderation_audit_logs (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES moderation_tasks(id) ON DELETE SET NULL,
    moderator_id UUID NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    previous_state JSONB,
    new_state JSONB,
    reason TEXT,
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
