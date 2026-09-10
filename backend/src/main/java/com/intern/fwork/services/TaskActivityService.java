package com.intern.fwork.services;

import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.entities.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Map;
import java.util.UUID;

public interface TaskActivityService {

    void log(UUID boardId, User actor, String actionType, String targetType, UUID targetId, String description, Map<String, Object> metadata);

    Page<TaskActivityResponse> getByBoard(UUID boardId, Pageable pageable, String actionType);
}
