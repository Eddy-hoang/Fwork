package com.intern.fwork.specifications;

import com.intern.fwork.entities.Task;
import com.intern.fwork.enums.Priority;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskSpecification {

    private TaskSpecification() {}

    public static Specification<Task> forBoard(UUID boardId) {
        return (root, query, cb) ->
                cb.equal(root.get("column").get("board").get("id"), boardId);
    }

    public static Specification<Task> notArchived() {
        return (root, query, cb) -> cb.isFalse(root.get("isArchived"));
    }

    public static Specification<Task> boardNotArchived() {
        return (root, query, cb) -> cb.isFalse(root.get("column").get("board").get("isArchived"));
    }

    public static Specification<Task> workspaceNotArchived() {
        return (root, query, cb) ->
                cb.isFalse(root.get("column").get("board").get("workspace").get("isArchived"));
    }

    public static Specification<Task> withKeyword(String q) {
        return (root, query, cb) -> {
            String pattern = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Task> withPriority(Priority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> withAssignee(UUID assigneeId) {
        return (root, query, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> withLabel(UUID labelId) {
        return (root, query, cb) -> {
            Join<Object, Object> labels = root.join("labels", JoinType.INNER);
            return cb.equal(labels.get("id"), labelId);
        };
    }

    public static Specification<Task> isOverdue() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("dueDate")),
                cb.lessThan(root.get("dueDate"), LocalDateTime.now())
        );
    }
}
