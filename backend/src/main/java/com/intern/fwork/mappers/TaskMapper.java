package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.TaskResponse;
import com.intern.fwork.entities.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .position(task.getPosition())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .columnId(task.getColumn() != null ? task.getColumn().getId() : null)
                .createdBy(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
                .updatedBy(task.getUpdatedBy() != null ? task.getUpdatedBy().getId() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .isArchived(task.isArchived())
                .build();
    }
}
