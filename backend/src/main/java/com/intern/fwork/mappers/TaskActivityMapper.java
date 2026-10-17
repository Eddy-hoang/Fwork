package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.entities.TaskActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskActivityMapper {

    public TaskActivityResponse toResponse(TaskActivity activity) {
        var actor = activity.getActor();
        return TaskActivityResponse.builder()
                .id(activity.getId())
                .boardId(activity.getBoardId())
                .actorId(actor != null ? actor.getId() : null)
                .actorName(actor != null ? actor.getName() : "Người dùng")
                .actorAvatar(actor != null ? actor.getAvatar() : null)
                .actionType(activity.getActionType())
                .targetType(activity.getTargetType())
                .targetId(activity.getTargetId())
                .description(activity.getDescription())
                .metadata(activity.getMetadata())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
