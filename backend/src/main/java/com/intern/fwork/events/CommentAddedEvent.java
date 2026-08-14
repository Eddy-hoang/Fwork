package com.intern.fwork.events;

import com.intern.fwork.entities.Comment;
import com.intern.fwork.entities.User;
import lombok.Getter;

@Getter
public class CommentAddedEvent {
    private final Comment comment;
    private final User actor;

    public CommentAddedEvent(Comment comment, User actor) {
        this.comment = comment;
        this.actor = actor;
    }
}
