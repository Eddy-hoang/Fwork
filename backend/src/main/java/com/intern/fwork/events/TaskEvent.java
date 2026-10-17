package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import lombok.Getter;

import java.util.UUID;

@Getter
public abstract class TaskEvent {
    private final Task task;
    private final User actor;
    /**
     * boardId is resolved eagerly within the originating transaction so that
     * @Async listeners can safely read it without traversing the LAZY
     * task → column → board chain on a closed Hibernate Session.
     */
    private final UUID boardId;

    protected TaskEvent(Task task, User actor) {
        this.task = task;
        this.actor = actor;
        // Resolve boardId eagerly while the JPA session is still open
        this.boardId = (task != null && task.getColumn() != null && task.getColumn().getBoard() != null)
                ? task.getColumn().getBoard().getId()
                : null;
    }
}

