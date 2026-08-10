package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.response.BoardDashboardResponse;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.BoardColumn;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.Priority;
import com.intern.fwork.exceptions.BoardNotFoundException;
import com.intern.fwork.repositories.BoardColumnRepository;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.BoardDashboardService;
import com.intern.fwork.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BoardDashboardServiceImpl implements BoardDashboardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Override
    public BoardDashboardResponse getDashboard(UUID boardId) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkWorkspaceAccess(board.getWorkspace().getId(), currentUser.getId());

        List<Task> tasks = taskRepository.findByBoardId(boardId);
        LocalDateTime now = LocalDateTime.now();

        long totalTasks = tasks.size();

        // Tasks by priority (include all enum values even if count = 0)
        Map<Priority, Long> tasksByPriority = Arrays.stream(Priority.values())
                .collect(Collectors.toMap(
                        p -> p,
                        p -> tasks.stream().filter(t -> t.getPriority() == p).count()
                ));

        // Tasks by column
        List<BoardColumn> columns = boardColumnRepository.findByBoardIdOrderByPositionAsc(boardId);
        List<BoardDashboardResponse.ColumnTaskCount> tasksByColumn = columns.stream()
                .map(col -> {
                    long count = tasks.stream()
                            .filter(t -> t.getColumn().getId().equals(col.getId()))
                            .count();
                    return BoardDashboardResponse.ColumnTaskCount.builder()
                            .columnId(col.getId())
                            .columnName(col.getName())
                            .taskCount(count)
                            .build();
                })
                .toList();

        long overdueTasks = tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now))
                .count();

        long unassignedTasks = tasks.stream()
                .filter(t -> t.getAssignee() == null)
                .count();

        return BoardDashboardResponse.builder()
                .boardId(board.getId())
                .boardTitle(board.getTitle())
                .totalTasks(totalTasks)
                .tasksByPriority(tasksByPriority)
                .tasksByColumn(tasksByColumn)
                .overdueTasks(overdueTasks)
                .unassignedTasks(unassignedTasks)
                .build();
    }
}
