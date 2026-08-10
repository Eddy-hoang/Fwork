package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.TaskActivity;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.TaskActivityAction;
import com.intern.fwork.exceptions.TaskNotFoundException;
import com.intern.fwork.mappers.TaskActivityMapper;
import com.intern.fwork.repositories.TaskActivityRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.PermissionService;
import com.intern.fwork.services.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;
    private final TaskActivityMapper taskActivityMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Override
    public void log(Task task, User actor, TaskActivityAction action, String detail) {
        TaskActivity activity = TaskActivity.builder()
                .task(task)
                .actor(actor)
                .action(action)
                .detail(detail)
                .build();
        taskActivityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskActivityResponse> getByTask(UUID taskId) {
        User currentUser = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .filter(t -> !t.isArchived() && !t.getColumn().getBoard().isArchived()
                        && !t.getColumn().getBoard().getWorkspace().isArchived())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        permissionService.checkWorkspaceAccess(
                task.getColumn().getBoard().getWorkspace().getId(), currentUser.getId());

        return taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(taskActivityMapper::toResponse)
                .toList();
    }
}
