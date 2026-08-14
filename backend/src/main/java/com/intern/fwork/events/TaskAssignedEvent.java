package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import lombok.Getter;
import java.util.UUID;

@Getter
public class TaskAssignedEvent extends TaskEvent {
    private final UUID assigneeId;

    public TaskAssignedEvent(Task task, User actor, UUID assigneeId) {
        super(task, actor);
        this.assigneeId = assigneeId;
    }
}
