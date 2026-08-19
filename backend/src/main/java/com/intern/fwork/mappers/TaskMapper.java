package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.TaskResponse;
import com.intern.fwork.entities.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final LabelMapper labelMapper;

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
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .labels(task.getLabels() != null
                        ? task.getLabels().stream().map(labelMapper::toResponse).toList()
                        : List.of())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .isArchived(task.isArchived())
                .isCompleted(task.isCompleted())
                .build();
    }
}
