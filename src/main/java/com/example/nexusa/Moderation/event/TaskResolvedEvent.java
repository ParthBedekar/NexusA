package com.example.nexusa.Moderation.event;

import java.util.UUID;
import org.springframework.context.ApplicationEvent;

public class TaskResolvedEvent extends ApplicationEvent {
    private final UUID taskId;
    private final UUID assigneId;
    private final String action;

    public TaskResolvedEvent(Object source, UUID taskId, UUID assigneId, String action) {
        super(source);
        this.taskId = taskId;
        this.assigneId = assigneId;
        this.action = action;
    }

    public UUID getTaskId() { return taskId; }
    public UUID getAssigneId() { return assigneId; }
    public String getAction() { return action; }
}
