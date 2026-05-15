package com.example.nexusa.Moderation.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ModerationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ModerationEventListener.class);

    @Async
    @EventListener
    public void handleTaskResolvedEvent(TaskResolvedEvent event) {
        log.info("Task {} resolved by user {}. Action: {}. Initiating notification to claimant...", 
                 event.getTaskId(), event.getAssigneId(), event.getAction());
        // Integrated notification service call goes here (e.g. email, websocket push)
    }
}
