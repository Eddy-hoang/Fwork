package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;

public class TaskUpdatedEvent extends TaskEvent {
    public TaskUpdatedEvent(Task task, User actor) {
        super(task, actor);
    }
}
