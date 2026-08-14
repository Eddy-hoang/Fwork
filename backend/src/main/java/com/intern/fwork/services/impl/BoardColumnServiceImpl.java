package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.CreateBoardColumnRequest;
import com.intern.fwork.dtos.request.UpdateBoardColumnRequest;
import com.intern.fwork.dtos.response.BoardColumnResponse;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.BoardColumn;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.exceptions.BoardColumnNotFoundException;
import com.intern.fwork.exceptions.BoardNotFoundException;
import com.intern.fwork.mappers.BoardColumnMapper;
import com.intern.fwork.repositories.BoardColumnRepository;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.CommentRepository;
import com.intern.fwork.repositories.TaskActivityRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.BoardColumnService;
import com.intern.fwork.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardColumnServiceImpl implements BoardColumnService {

    private final BoardColumnRepository boardColumnRepository;
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final BoardColumnMapper boardColumnMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Autowired
    private CacheManager cacheManager;

    private void evictDashboardCache(UUID boardId) {
        if (cacheManager.getCache("dashboard") != null) {
            cacheManager.getCache("dashboard").evict(boardId);
        }
    }

    @Override
    @CacheEvict(value = "dashboard", key = "#boardId")
    public BoardColumnResponse create(UUID boardId, CreateBoardColumnRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkCreateColumn(boardId, currentUser.getId());

        int position = request.getPosition() != null
                ? request.getPosition()
                : boardColumnRepository.findByBoardIdOrderByPositionAsc(boardId).size();

        BoardColumn column = BoardColumn.builder()
                .name(request.getName())
                .position(position)
                .board(board)
                .build();

        return boardColumnMapper.toResponse(boardColumnRepository.save(column));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnResponse> getAllByBoard(UUID boardId) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkWorkspaceAccess(board.getWorkspace().getId(), currentUser.getId());

        List<BoardColumn> columns = boardColumnRepository.findByBoardIdOrderByPositionAsc(boardId);

        return columns.stream()
                .map(boardColumnMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardColumnResponse getById(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        BoardColumn column = boardColumnRepository.findById(id)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        if (column.getBoard().isArchived() || column.getBoard().getWorkspace().isArchived()) {
            throw new BoardColumnNotFoundException("Column not found");
        }

        permissionService.checkWorkspaceAccess(column.getBoard().getWorkspace().getId(), currentUser.getId());

        return boardColumnMapper.toResponse(column);
    }

    @Override
    public BoardColumnResponse update(UUID id, UpdateBoardColumnRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        BoardColumn column = boardColumnRepository.findById(id)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        if (column.getBoard().isArchived() || column.getBoard().getWorkspace().isArchived()) {
            throw new BoardColumnNotFoundException("Column not found");
        }

        permissionService.checkUpdateColumn(id, currentUser.getId());

        column.setName(request.getName());
        if (request.getPosition() != null) {
            column.setPosition(request.getPosition());
        }

        BoardColumnResponse response = boardColumnMapper.toResponse(boardColumnRepository.save(column));

        evictDashboardCache(column.getBoard().getId());

        return response;
    }

    @Override
    public void delete(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        BoardColumn column = boardColumnRepository.findById(id)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));

        if (column.getBoard().isArchived() || column.getBoard().getWorkspace().isArchived()) {
            throw new BoardColumnNotFoundException("Column not found");
        }

        permissionService.checkDeleteColumn(id, currentUser.getId());

        UUID boardId = column.getBoard().getId();

        // 1. Delete comments, task activities, and labels for all tasks in this column to prevent Foreign Key violations
        List<Task> tasks = taskRepository.findByColumnId(id);
        for (Task t : tasks) {
            commentRepository.deleteByTaskId(t.getId());
            taskActivityRepository.deleteByTaskId(t.getId());
            t.getLabels().clear();
        }
        taskRepository.saveAll(tasks);
        taskRepository.deleteAll(tasks);

        // 2. Delete the column
        boardColumnRepository.delete(column);

        evictDashboardCache(boardId);
    }
}
