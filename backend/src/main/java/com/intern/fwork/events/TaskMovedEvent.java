package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import lombok.Getter;
import java.util.UUID;

@Getter
public class TaskMovedEvent extends TaskEvent {
    private final UUID sourceColumnId;
    private final UUID targetColumnId;

    public TaskMovedEvent(Task task, User actor, UUID sourceColumnId, UUID targetColumnId) {
        super(task, actor);
        this.sourceColumnId = sourceColumnId;
        this.targetColumnId = targetColumnId;
    }
}
