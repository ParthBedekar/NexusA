package com.example.nexusa.Moderation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "moderation_audit_logs")
public class ModerationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id")
    private UUID taskId;

    @Column(name = "moderator_id", nullable = false)
    private UUID moderatorId;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "previous_state", columnDefinition = "jsonb")
    private String previousState;

    @Column(name = "new_state", columnDefinition = "jsonb")
    private String newState;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getTaskId() { return taskId; }
    public void setTaskId(UUID taskId) { this.taskId = taskId; }
    
    public UUID getModeratorId() { return moderatorId; }
    public void setModeratorId(UUID moderatorId) { this.moderatorId = moderatorId; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getPreviousState() { return previousState; }
    public void setPreviousState(String previousState) { this.previousState = previousState; }
    
    public String getNewState() { return newState; }
    public void setNewState(String newState) { this.newState = newState; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
