package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.entities.TaskActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskActivityMapper {

    public TaskActivityResponse toResponse(TaskActivity activity) {
        return TaskActivityResponse.builder()
                .id(activity.getId())
                .boardId(activity.getBoardId())
                .actorId(activity.getActor().getId())
                .actorName(activity.getActor().getName())
                .actorAvatar(activity.getActor().getAvatar())
                .actionType(activity.getActionType())
                .targetType(activity.getTargetType())
                .targetId(activity.getTargetId())
                .description(activity.getDescription())
                .metadata(activity.getMetadata())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
