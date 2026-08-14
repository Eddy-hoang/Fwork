package com.intern.fwork.events;

import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.entities.Label;
import lombok.Getter;
import java.util.Collection;

@Getter
public class LabelUpdatedEvent extends TaskEvent {
    private final Collection<Label> labels;

    public LabelUpdatedEvent(Task task, User actor, Collection<Label> labels) {
        super(task, actor);
        this.labels = labels;
    }
}
