package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import lombok.Getter;

@Getter
public abstract class TaskEvent {
    private final Task task;
    private final User actor;

    protected TaskEvent(Task task, User actor) {
        this.task = task;
        this.actor = actor;
    }
}
