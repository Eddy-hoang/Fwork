package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;

public class TaskCreatedEvent extends TaskEvent {
    public TaskCreatedEvent(Task task, User actor) {
        super(task, actor);
    }
}
