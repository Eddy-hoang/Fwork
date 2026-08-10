package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.entities.TaskActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskActivityMapper {

    private final UserMapper userMapper;

    public TaskActivityResponse toResponse(TaskActivity activity) {
        return TaskActivityResponse.builder()
                .id(activity.getId())
                .taskId(activity.getTask().getId())
                .actor(userMapper.toResponse(activity.getActor()))
                .action(activity.getAction())
                .detail(activity.getDetail())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
