package com.example.nexusa.Moderation.service;

import com.example.nexusa.Dto.GlobalDTOs.ModerationActionRequestDTO;
import com.example.nexusa.Dto.GlobalDTOs.ModerationTaskDetailDTO;
import com.example.nexusa.Dto.GlobalDTOs.ModerationTaskListDTO;
import com.example.nexusa.Moderation.entity.ModerationAuditLog;
import com.example.nexusa.Moderation.entity.ModerationTask;
import com.example.nexusa.Moderation.entity.ModerationTask.TaskStatus;
import com.example.nexusa.Repository.GlobalRepositories.ModerationAuditLogRepository;
import com.example.nexusa.Repository.GlobalRepositories.ModerationTaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ModerationService {

    private final ModerationTaskRepository taskRepository;
    private final ModerationAuditLogRepository auditLogRepository;

    public ModerationService(ModerationTaskRepository taskRepository, ModerationAuditLogRepository auditLogRepository) {
        this.taskRepository = taskRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public Page<ModerationTaskListDTO> getTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(task -> new ModerationTaskListDTO(
                task.getId(),
                task.getTargetType(),
                "Review needed for " + task.getTargetType().name(),
                task.getStatus(),
                task.getAssigneeId(),
                task.getPriority(),
                task.getCreatedAt()
        ));
    }

    public ModerationTaskDetailDTO getTaskDetail(UUID taskId) {
        ModerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        ModerationTaskDetailDTO dto = new ModerationTaskDetailDTO();
        dto.setTaskId(task.getId());
        dto.setStatus(task.getStatus());
        // In a real implementation, you would fetch domain objects (Claims, Conflicts, Entities) 
        // based on the targetId and map them to the Summary classes.
        return dto;
    }

    @Transactional
    public void assignTask(UUID taskId, UUID assigneeId) {
        ModerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        task.setAssigneeId(assigneeId);
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        logAction(taskId, assigneeId, "ASSIGN_TASK", "Task assigned to user", "assigned");
    }

    @Transactional
    public void executeAction(UUID taskId, UUID moderatorId, ModerationActionRequestDTO request) {
        ModerationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        
        // Handle logic for APPROVE, REJECT, MERGE
        // This is a stub for the core logic integrating with other modules.
        
        task.setStatus(TaskStatus.RESOLVED);
        task.setResolvedAt(LocalDateTime.now());
        taskRepository.save(task);

        logAction(taskId, moderatorId, request.getAction(), request.getRationale(), "resolved");
    }

    private void logAction(UUID taskId, UUID moderatorId, String actionType, String reason, String newState) {
        ModerationAuditLog log = new ModerationAuditLog();
        log.setTaskId(taskId);
        log.setModeratorId(moderatorId);
        log.setActionType(actionType);
        log.setReason(reason);
        log.setNewState(newState); // Normally a JSON string describing the new state
        auditLogRepository.save(log);
    }
}
