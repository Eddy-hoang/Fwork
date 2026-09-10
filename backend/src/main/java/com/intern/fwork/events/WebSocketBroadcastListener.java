package com.intern.fwork.events;

import com.intern.fwork.dtos.websocket.WebSocketEvent;
import com.intern.fwork.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketBroadcastListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_CREATED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), Map.of("taskTitle", event.getTask().getTitle()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_UPDATED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), Map.of("taskTitle", event.getTask().getTitle()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMoved(TaskMovedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_MOVED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), Map.of("taskTitle", event.getTask().getTitle()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_ASSIGNED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), Map.of("taskTitle", event.getTask().getTitle()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLabelUpdated(LabelUpdatedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "LABELS_UPDATED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), Map.of("taskTitle", event.getTask().getTitle()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentAdded(CommentAddedEvent event) {
        UUID boardId = resolveCommentBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "COMMENT_ADDED", userMapper.toResponse(event.getActor()), "COMMENT", event.getComment().getId(), Map.of("taskId", event.getComment().getTask().getId()));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        UUID boardId = resolveCommentDeletedBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "COMMENT_DELETED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), Map.of("taskId", event.getTask().getId()));
    }

    private void broadcast(UUID boardId, String eventType, com.intern.fwork.dtos.response.UserResponse actor, String targetType, UUID targetId, Map<String, Object> data) {
        WebSocketEvent wsEvent = WebSocketEvent.builder()
                .eventId("evt-" + UUID.randomUUID())
                .eventType(eventType)
                .version(1)
                .boardId(boardId)
                .actor(actor)
                .target(new WebSocketEvent.TargetRef(targetType, targetId))
                .data(data)
                .occurredAt(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend("/topic/boards/" + boardId, wsEvent);
    }

    private UUID resolveBoardId(TaskEvent event) {
        try {
            return event.getTask().getColumn().getBoard().getId();
        } catch (Exception ex) {
            return null;
        }
    }

    private UUID resolveCommentBoardId(CommentAddedEvent event) {
        try {
            return event.getComment().getTask().getColumn().getBoard().getId();
        } catch (Exception ex) {
            return null;
        }
    }

    private UUID resolveCommentDeletedBoardId(CommentDeletedEvent event) {
        try {
            return event.getTask().getColumn().getBoard().getId();
        } catch (Exception ex) {
            return null;
        }
    }
}
