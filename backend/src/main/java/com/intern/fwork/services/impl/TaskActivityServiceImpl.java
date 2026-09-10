package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.TaskActivity;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.TaskActivityAction;
import com.intern.fwork.exceptions.ResourceNotFoundException;
import com.intern.fwork.mappers.TaskActivityMapper;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.TaskActivityRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.PermissionService;
import com.intern.fwork.services.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;
    private final BoardRepository boardRepository;
    private final TaskActivityMapper taskActivityMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Override
    public void log(UUID boardId, User actor, String actionType, String targetType, UUID targetId, String description, Map<String, Object> metadata) {
        TaskActivity activity = TaskActivity.builder()
                .boardId(boardId)
                .actor(actor)
                .actionType(actionType)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .metadata(metadata)
                .build();
        taskActivityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskActivityResponse> getByBoard(UUID boardId, Pageable pageable, String actionType) {
        User currentUser = securityUtils.getCurrentUser();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        permissionService.checkWorkspaceAccess(board.getWorkspace().getId(), currentUser.getId());

        Page<TaskActivity> page;
        if (actionType != null && !actionType.isBlank()) {
            page = taskActivityRepository.findByBoardIdAndActionTypeOrderByCreatedAtDesc(boardId, actionType, pageable);
        } else {
            page = taskActivityRepository.findByBoardIdOrderByCreatedAtDesc(boardId, pageable);
        }

        return page.map(taskActivityMapper::toResponse);
    }
}
