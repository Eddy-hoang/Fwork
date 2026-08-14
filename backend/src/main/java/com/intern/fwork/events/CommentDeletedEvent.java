package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import lombok.Getter;

@Getter
public class CommentDeletedEvent {
    private final Task task;
    private final User actor;

    public CommentDeletedEvent(Task task, User actor) {
        this.task = task;
        this.actor = actor;
    }
}
