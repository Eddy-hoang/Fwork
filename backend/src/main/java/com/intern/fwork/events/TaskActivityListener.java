package com.intern.fwork.events;

import com.intern.fwork.services.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskActivityListener {

    private final TaskActivityService taskActivityService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        if (event.getTask() == null || event.getTask().getColumn() == null) return;
        taskActivityService.log(
                event.getTask().getColumn().getBoard().getId(),
                event.getActor(),
                "TASK_CREATED",
                "TASK",
                event.getTask().getId(),
                event.getActor().getName() + " vừa tạo thẻ '" + event.getTask().getTitle() + "'",
                Map.of("taskTitle", event.getTask().getTitle())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        if (event.getTask() == null || event.getTask().getColumn() == null) return;
        taskActivityService.log(
                event.getTask().getColumn().getBoard().getId(),
                event.getActor(),
                "TASK_UPDATED",
                "TASK",
                event.getTask().getId(),
                event.getActor().getName() + " vừa cập nhật thẻ '" + event.getTask().getTitle() + "'",
                Map.of("taskTitle", event.getTask().getTitle())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMoved(TaskMovedEvent event) {
        if (event.getTask() == null || event.getTask().getColumn() == null) return;
        taskActivityService.log(
                event.getTask().getColumn().getBoard().getId(),
                event.getActor(),
                "TASK_MOVED",
                "TASK",
                event.getTask().getId(),
                event.getActor().getName() + " vừa di chuyển thẻ '" + event.getTask().getTitle() + "'",
                Map.of("taskTitle", event.getTask().getTitle())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        if (event.getTask() == null || event.getTask().getColumn() == null) return;
        taskActivityService.log(
                event.getTask().getColumn().getBoard().getId(),
                event.getActor(),
                "TASK_ASSIGNED",
                "TASK",
                event.getTask().getId(),
                event.getActor().getName() + " vừa gán thẻ '" + event.getTask().getTitle() + "'",
                Map.of("taskTitle", event.getTask().getTitle())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentAdded(CommentAddedEvent event) {
        if (event.getComment() == null || event.getComment().getTask() == null || event.getComment().getTask().getColumn() == null) return;
        taskActivityService.log(
                event.getComment().getTask().getColumn().getBoard().getId(),
                event.getActor(),
                "COMMENT_ADDED",
                "COMMENT",
                event.getComment().getId(),
                event.getActor().getName() + " vừa bình luận vào thẻ '" + event.getComment().getTask().getTitle() + "'",
                Map.of("taskId", event.getComment().getTask().getId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        if (event.getTask() == null || event.getTask().getColumn() == null) return;
        taskActivityService.log(
                event.getTask().getColumn().getBoard().getId(),
                event.getActor(),
                "COMMENT_DELETED",
                "TASK",
                event.getTask().getId(),
                event.getActor().getName() + " vừa xóa bình luận trong thẻ '" + event.getTask().getTitle() + "'",
                Map.of("taskId", event.getTask().getId())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLabelUpdated(LabelUpdatedEvent event) {
        if (event.getTask() == null || event.getTask().getColumn() == null) return;
        taskActivityService.log(
                event.getTask().getColumn().getBoard().getId(),
                event.getActor(),
                "LABELS_UPDATED",
                "TASK",
                event.getTask().getId(),
                event.getActor().getName() + " vừa cập nhật nhãn cho thẻ '" + event.getTask().getTitle() + "'",
                Map.of("taskTitle", event.getTask().getTitle())
        );
    }
}
