package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.AssignTaskRequest;
import com.intern.fwork.dtos.request.CreateTaskRequest;
import com.intern.fwork.dtos.request.MoveTaskRequest;
import com.intern.fwork.dtos.request.UpdateTaskRequest;
import com.intern.fwork.dtos.response.TaskResponse;
import com.intern.fwork.exceptions.UserNotFoundException;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.BoardColumn;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.exceptions.BoardColumnNotFoundException;
import com.intern.fwork.exceptions.BoardNotFoundException;
import com.intern.fwork.exceptions.TaskNotFoundException;
import com.intern.fwork.mappers.TaskMapper;
import com.intern.fwork.repositories.BoardColumnRepository;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.repositories.UserRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.PermissionService;
import com.intern.fwork.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Override
    public TaskResponse create(UUID columnId, CreateTaskRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        if (column.getBoard().isArchived() || column.getBoard().getWorkspace().isArchived()) {
            throw new BoardColumnNotFoundException("Column not found");
        }

        permissionService.checkCreateTask(columnId, currentUser.getId());

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .position(request.getPosition())
                .column(column)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .isArchived(false)
                .build();

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByColumn(UUID columnId) {
        User currentUser = securityUtils.getCurrentUser();

        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        if (column.getBoard().isArchived() || column.getBoard().getWorkspace().isArchived()) {
            throw new BoardColumnNotFoundException("Column not found");
        }

        permissionService.checkWorkspaceAccess(column.getBoard().getWorkspace().getId(), currentUser.getId());

        List<Task> tasks = taskRepository.findByColumnIdAndIsArchivedFalseOrderByPositionAsc(columnId);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByBoard(UUID boardId) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkWorkspaceAccess(board.getWorkspace().getId(), currentUser.getId());

        List<Task> tasks = taskRepository.findByBoardId(boardId);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getById(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(id)
                .filter(t -> !t.isArchived())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (task.getColumn().getBoard().isArchived() || task.getColumn().getBoard().getWorkspace().isArchived()) {
            throw new TaskNotFoundException("Task not found");
        }

        permissionService.checkWorkspaceAccess(task.getColumn().getBoard().getWorkspace().getId(), currentUser.getId());

        return taskMapper.toResponse(task);
    }

    @Override
    public TaskResponse update(UUID id, UpdateTaskRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(id)
                .filter(t -> !t.isArchived())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (task.getColumn().getBoard().isArchived() || task.getColumn().getBoard().getWorkspace().isArchived()) {
            throw new TaskNotFoundException("Task not found");
        }

        permissionService.checkUpdateTask(id, currentUser.getId());

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());
        task.setUpdatedBy(currentUser);

        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    public void delete(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(id)
                .filter(t -> !t.isArchived())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (task.getColumn().getBoard().isArchived() || task.getColumn().getBoard().getWorkspace().isArchived()) {
            throw new TaskNotFoundException("Task not found");
        }

        permissionService.checkDeleteTask(id, currentUser.getId());

        task.setArchived(true);
        task.setUpdatedBy(currentUser);
        taskRepository.save(task);
    }

    @Override
    public TaskResponse move(UUID id, MoveTaskRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Task task = taskRepository.findById(id)
                .filter(t -> !t.isArchived())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        BoardColumn targetColumn = boardColumnRepository.findById(request.getTargetColumnId())
                .orElseThrow(() -> new BoardColumnNotFoundException("Target column not found"));

        if (targetColumn.getBoard().isArchived() || targetColumn.getBoard().getWorkspace().isArchived()) {
            throw new BoardColumnNotFoundException("Target column not found");
        }

        permissionService.checkUpdateTask(id, currentUser.getId());
        permissionService.checkWorkspaceAccess(targetColumn.getBoard().getWorkspace().getId(), currentUser.getId());

        UUID sourceColumnId = task.getColumn().getId();
        UUID targetColumnId = request.getTargetColumnId();

        if (sourceColumnId.equals(targetColumnId)) {
            List<Task> tasks = taskRepository.findByColumnIdAndIsArchivedFalseOrderByPositionAsc(sourceColumnId);
            tasks.removeIf(t -> t.getId().equals(task.getId()));

            int newPos = Math.max(0, Math.min(request.getTargetPosition(), tasks.size()));
            tasks.add(newPos, task);

            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).setPosition(i);
                taskRepository.save(tasks.get(i));
            }
        } else {
            // Source column re-indexing
            List<Task> sourceTasks = taskRepository.findByColumnIdAndIsArchivedFalseOrderByPositionAsc(sourceColumnId);
            sourceTasks.removeIf(t -> t.getId().equals(task.getId()));
            for (int i = 0; i < sourceTasks.size(); i++) {
                sourceTasks.get(i).setPosition(i);
                taskRepository.save(sourceTasks.get(i));
            }

            // Target column re-indexing
            List<Task> targetTasks = taskRepository.findByColumnIdAndIsArchivedFalseOrderByPositionAsc(targetColumnId);
            task.setColumn(targetColumn);
            int newPos = Math.max(0, Math.min(request.getTargetPosition(), targetTasks.size()));
            targetTasks.add(newPos, task);
            for (int i = 0; i < targetTasks.size(); i++) {
                targetTasks.get(i).setPosition(i);
                taskRepository.save(targetTasks.get(i));
            }
        }

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse assign(UUID id, AssignTaskRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        permissionService.checkAssignTask(id, request.getAssigneeId(), currentUser.getId());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new UserNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        task.setUpdatedBy(currentUser);
        return taskMapper.toResponse(taskRepository.save(task));
    }
}
