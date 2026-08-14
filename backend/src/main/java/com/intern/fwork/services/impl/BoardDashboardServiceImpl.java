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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
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

    @Autowired
    @Lazy
    private BoardDashboardService self;

    @Override
    public BoardDashboardResponse getDashboard(UUID boardId) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkWorkspaceAccess(board.getWorkspace().getId(), currentUser.getId());

        return self.getDashboardCacheData(boardId);
    }

    @Override
    @Cacheable(value = "dashboard", key = "#boardId")
    public BoardDashboardResponse getDashboardCacheData(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        LocalDateTime now = LocalDateTime.now();

        long totalTasks = taskRepository.countByColumnBoardIdAndIsArchivedFalse(boardId);

        // Tasks by priority (include all enum values even if count = 0)
        List<Object[]> priorityCounts = taskRepository.countByPriorityForBoard(boardId);
        Map<Priority, Long> priorityCountsMap = priorityCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (Priority) arr[0],
                        arr -> (Long) arr[1]
                ));
        Map<Priority, Long> tasksByPriority = Arrays.stream(Priority.values())
                .collect(Collectors.toMap(
                        p -> p,
                        p -> priorityCountsMap.getOrDefault(p, 0L)
                ));

        // Tasks by column
        List<Object[]> columnCounts = taskRepository.countByColumnForBoard(boardId);
        Map<UUID, Long> columnCountsMap = columnCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (UUID) arr[0],
                        arr -> (Long) arr[1]
                ));
        List<BoardColumn> columns = boardColumnRepository.findByBoardIdOrderByPositionAsc(boardId);
        List<BoardDashboardResponse.ColumnTaskCount> tasksByColumn = columns.stream()
                .map(col -> BoardDashboardResponse.ColumnTaskCount.builder()
                        .columnId(col.getId())
                        .columnName(col.getName())
                        .taskCount(columnCountsMap.getOrDefault(col.getId(), 0L))
                        .build())
                .toList();

        long overdueTasks = taskRepository.countByColumnBoardIdAndIsArchivedFalseAndDueDateIsNotNullAndDueDateBefore(boardId, now);

        long unassignedTasks = taskRepository.countByColumnBoardIdAndIsArchivedFalseAndAssigneeIsNull(boardId);

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
