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
import java.util.UUID;

/**
 * Listens to domain events and broadcasts structured WebSocketEvent payloads
 * to the appropriate /topic/boards/{boardId} STOMP channel.
 *
 * All handlers run @Async and after the main transaction commits to prevent
 * broadcast of events that were rolled back.
 */
@Component
@RequiredArgsConstructor
public class WebSocketBroadcastListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    // ──────────────────────────────────────────────────────────────────────────
    // Task Events
    // ──────────────────────────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_CREATED", event.getActor().getId(), userMapper.toResponse(event.getActor()), event.getTask().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_UPDATED", event.getActor().getId(), userMapper.toResponse(event.getActor()), event.getTask().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMoved(TaskMovedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_MOVED", event.getActor().getId(), userMapper.toResponse(event.getActor()), event.getTask().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "TASK_ASSIGNED", event.getActor().getId(), userMapper.toResponse(event.getActor()), event.getTask().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLabelUpdated(LabelUpdatedEvent event) {
        UUID boardId = resolveBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "LABELS_UPDATED", event.getActor().getId(), userMapper.toResponse(event.getActor()), event.getTask().getId());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Comment Events
    // ──────────────────────────────────────────────────────────────────────────

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentAdded(CommentAddedEvent event) {
        UUID boardId = resolveCommentBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "COMMENT_ADDED", event.getActor().getId(), userMapper.toResponse(event.getActor()), event.getComment().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        UUID boardId = resolveCommentDeletedBoardId(event);
        if (boardId == null) return;
        broadcast(boardId, "COMMENT_DELETED", event.getActor().getId(), userMapper.toResponse(event.getActor()), event.getTask().getId());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void broadcast(UUID boardId, String type, UUID actorId, Object actorDto, Object payload) {
        WebSocketEvent wsEvent = WebSocketEvent.builder()
                .type(type)
                .boardId(boardId)
                .actor((com.intern.fwork.dtos.response.UserResponse) actorDto)
                .payload(payload)
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
