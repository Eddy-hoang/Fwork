package com.intern.fwork.events;

import com.intern.fwork.entities.Comment;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.NotificationType;
import com.intern.fwork.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskAssigned(TaskAssignedEvent event) {
        User assignee = event.getTask().getAssignee();
        User actor = event.getActor();
        if (assignee != null && !assignee.getId().equals(actor.getId())) {
            notificationService.createNotification(
                    assignee,
                    actor,
                    NotificationType.TASK_ASSIGNED,
                    "Bạn đã được giao nhiệm vụ mới",
                    "Bạn đã được giao nhiệm vụ '" + event.getTask().getTitle() + "' bởi " + actor.getName(),
                    "TASK",
                    event.getTask().getId()
            );
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentAdded(CommentAddedEvent event) {
        Comment comment = event.getComment();
        Task task = comment.getTask();
        User creator = task.getCreatedBy();
        User assignee = task.getAssignee();
        User commenter = event.getActor();

        Set<User> recipients = new HashSet<>();
        if (creator != null && !creator.getId().equals(commenter.getId())) {
            recipients.add(creator);
        }
        if (assignee != null && !assignee.getId().equals(commenter.getId())) {
            recipients.add(assignee);
        }

        for (User recipient : recipients) {
            notificationService.createNotification(
                    recipient,
                    commenter,
                    NotificationType.COMMENT_ADDED,
                    "Bình luận mới trên nhiệm vụ",
                    commenter.getName() + " đã thêm bình luận mới trên nhiệm vụ '" + task.getTitle() + "'",
                    "TASK",
                    task.getId()
            );
        }
    }
}
