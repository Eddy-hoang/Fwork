package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.TaskActivityAction;
import com.intern.fwork.services.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TaskActivityListener {

    private final TaskActivityService taskActivityService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        taskActivityService.log(event.getTask(), event.getActor(), TaskActivityAction.TASK_CREATED,
                "Task '" + event.getTask().getTitle() + "' created");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskUpdated(TaskUpdatedEvent event) {
        taskActivityService.log(event.getTask(), event.getActor(), TaskActivityAction.TASK_UPDATED,
                "Task updated: title='" + event.getTask().getTitle() + "'");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskMoved(TaskMovedEvent event) {
        taskActivityService.log(event.getTask(), event.getActor(), TaskActivityAction.TASK_MOVED,
                "Task moved to columnId=" + event.getTargetColumnId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        String detail = event.getAssigneeId() != null
                ? "Assigned to userId=" + event.getAssigneeId()
                : "Unassigned";
        TaskActivityAction action = event.getAssigneeId() != null
                ? TaskActivityAction.TASK_ASSIGNED
                : TaskActivityAction.TASK_UNASSIGNED;
        taskActivityService.log(event.getTask(), event.getActor(), action, detail);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentAdded(CommentAddedEvent event) {
        taskActivityService.log(event.getComment().getTask(), event.getActor(), TaskActivityAction.COMMENT_ADDED,
                "Comment added by " + event.getActor().getName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        taskActivityService.log(event.getTask(), event.getActor(), TaskActivityAction.COMMENT_DELETED,
                "Comment deleted by " + event.getActor().getName());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLabelUpdated(LabelUpdatedEvent event) {
        taskActivityService.log(event.getTask(), event.getActor(), TaskActivityAction.LABELS_UPDATED,
                "Labels updated: " + event.getLabels().stream().map(l -> l.getName()).toList());
    }
}
