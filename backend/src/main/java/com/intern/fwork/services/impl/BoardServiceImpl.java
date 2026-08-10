package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.CreateBoardRequest;
import com.intern.fwork.dtos.request.UpdateBoardRequest;
import com.intern.fwork.dtos.response.BoardResponse;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.User;
import com.intern.fwork.entities.Workspace;
import com.intern.fwork.exceptions.BoardNotFoundException;
import com.intern.fwork.exceptions.WorkspaceNotFoundException;
import com.intern.fwork.mappers.BoardMapper;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.WorkspaceRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.BoardService;
import com.intern.fwork.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final WorkspaceRepository workspaceRepository;
    private final BoardMapper boardMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Override
    public BoardResponse create(CreateBoardRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                .filter(w -> !w.isArchived())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        permissionService.checkCreateBoard(workspace.getId(), currentUser.getId());

        Board board = Board.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .color(request.getColor())
                .workspace(workspace)
                .isArchived(false)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .position(0) // Default position
                .build();

        return boardMapper.toResponse(boardRepository.save(board));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getMyBoards() {
        User currentUser = securityUtils.getCurrentUser();
        List<Board> boards = boardRepository.findAllByMemberUserId(currentUser.getId());
        return boards.stream()
                .map(boardMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getById(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(id)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkWorkspaceAccess(board.getWorkspace().getId(), currentUser.getId());

        return boardMapper.toResponse(board);
    }

    @Override
    public BoardResponse update(UUID id, UpdateBoardRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(id)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkUpdateBoard(id, currentUser.getId());

        board.setTitle(request.getTitle());
        board.setDescription(request.getDescription());
        board.setColor(request.getColor());
        board.setUpdatedBy(currentUser);

        return boardMapper.toResponse(boardRepository.save(board));
    }

    @Override
    public void delete(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        Board board = boardRepository.findById(id)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));

        permissionService.checkDeleteBoard(id, currentUser.getId());

        board.setArchived(true);
        board.setUpdatedBy(currentUser);
        boardRepository.save(board);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardsByWorkspaceId(UUID workspaceId) {
        User currentUser = securityUtils.getCurrentUser();

        permissionService.checkWorkspaceAccess(workspaceId, currentUser.getId());

        List<Board> boards = boardRepository.findByWorkspaceIdAndIsArchivedFalseOrderByPositionAsc(workspaceId);

        return boards.stream()
                .map(boardMapper::toResponse)
                .toList();
    }
}