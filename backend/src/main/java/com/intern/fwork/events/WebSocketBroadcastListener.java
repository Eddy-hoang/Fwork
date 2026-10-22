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
        // Use pre-resolved boardId — avoids LazyInitializationException on async thread
        if (event.getBoardId() == null || event.getTask() == null) return;
        Map<String, Object> taskData = buildTaskPayload(event.getTask(), null);
        broadcast(event.getBoardId(), "TASK_CREATED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), taskData);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        if (event.getBoardId() == null || event.getTask() == null) return;
        Map<String, Object> taskData = buildTaskPayload(event.getTask(), null);
        broadcast(event.getBoardId(), "TASK_UPDATED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), taskData);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMoved(TaskMovedEvent event) {
        if (event.getBoardId() == null || event.getTask() == null) return;
        Map<String, Object> taskData = buildTaskPayload(event.getTask(), event.getTargetColumnId());
        broadcast(event.getBoardId(), "TASK_MOVED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), taskData);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        if (event.getBoardId() == null || event.getTask() == null) return;
        Map<String, Object> taskData = buildTaskPayload(event.getTask(), null);
        if (event.getAssigneeId() != null) {
            taskData.put("assigneeId", event.getAssigneeId());
            taskData.put("assignee_id", event.getAssigneeId());
        }
        broadcast(event.getBoardId(), "TASK_ASSIGNED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), taskData);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLabelUpdated(LabelUpdatedEvent event) {
        if (event.getBoardId() == null || event.getTask() == null) return;
        Map<String, Object> taskData = buildTaskPayload(event.getTask(), null);
        broadcast(event.getBoardId(), "LABELS_UPDATED", userMapper.toResponse(event.getActor()), "TASK", event.getTask().getId(), taskData);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentAdded(CommentAddedEvent event) {
        UUID boardId = resolveCommentBoardId(event);
        if (boardId == null || event.getComment() == null) return;
        Map<String, Object> commentData = new java.util.HashMap<>();
        commentData.put("id", event.getComment().getId());
        commentData.put("content", event.getComment().getContent());
        if (event.getComment().getTask() != null) {
            commentData.put("taskId", event.getComment().getTask().getId());
        }
        if (event.getComment().getCreatedBy() != null) {
            commentData.put("createdBy", userMapper.toResponse(event.getComment().getCreatedBy()));
        }
        broadcast(boardId, "COMMENT_ADDED", userMapper.toResponse(event.getActor()), "COMMENT", event.getComment().getId(), commentData);
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
                .type(eventType)
                .version(1)
                .boardId(boardId)
                .actor(actor)
                .target(new WebSocketEvent.TargetRef(targetType, targetId))
                .data(data)
                .payload(data)
                .occurredAt(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend("/topic/boards/" + boardId, wsEvent);
    }

    private Map<String, Object> buildTaskPayload(com.intern.fwork.entities.Task task, UUID overrideColumnId) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", task.getId());
        map.put("title", task.getTitle());
        map.put("description", task.getDescription());
        map.put("position", task.getPosition());
        if (task.getPriority() != null) {
            map.put("priority", task.getPriority().name());
        }
        map.put("dueDate", task.getDueDate());
        map.put("due_date", task.getDueDate());
        map.put("isCompleted", task.isCompleted());
        map.put("isArchived", task.isArchived());

        UUID colId = overrideColumnId;
        if (colId == null && task.getColumn() != null) {
            try {
                colId = task.getColumn().getId();
            } catch (Exception ignored) {}
        }
        if (colId != null) {
            map.put("columnId", colId);
            map.put("column_id", colId);
        }

        if (task.getAssignee() != null) {
            try {
                map.put("assigneeId", task.getAssignee().getId());
                map.put("assignee_id", task.getAssignee().getId());
            } catch (Exception ignored) {}
        }
        return map;
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

