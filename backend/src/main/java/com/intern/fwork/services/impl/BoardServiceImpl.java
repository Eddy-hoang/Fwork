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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import com.intern.fwork.entities.WorkspaceMember;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.repositories.WorkspaceMemberRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardMapper boardMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;

    @Autowired
    @Lazy
    private BoardService self;

    @Autowired
    private CacheManager cacheManager;

    @Override
    public BoardResponse create(CreateBoardRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Workspace workspace;
        if (request.getWorkspaceId() != null) {
            workspace = workspaceRepository.findById(request.getWorkspaceId())
                    .filter(w -> !w.isArchived())
                    .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));
        } else {
            List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(currentUser.getId());
            if (!memberships.isEmpty()) {
                workspace = memberships.get(0).getWorkspace();
            } else {
                workspace = workspaceRepository.save(Workspace.builder()
                        .name(currentUser.getName() + "'s Workspace")
                        .slug("ws-" + UUID.randomUUID())
                        .createdBy(currentUser)
                        .updatedBy(currentUser)
                        .build());
                workspaceMemberRepository.save(WorkspaceMember.builder()
                        .workspace(workspace)
                        .user(currentUser)
                        .role(WorkspaceRole.OWNER)
                        .build());
            }
        }

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

        if (cacheManager.getCache("workspace") != null) {
            cacheManager.getCache("workspace").evict(workspace.getId());
        }

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

        BoardResponse response = self.getBoardCacheData(id);

        permissionService.checkWorkspaceAccess(response.getWorkspaceId(), currentUser.getId());

        return response;
    }

    @Override
    @CacheEvict(value = "board", key = "#id")
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

        // Evict workspace cache to update boardCount
        UUID workspaceId = board.getWorkspace().getId();
        if (cacheManager.getCache("workspace") != null) {
            cacheManager.getCache("workspace").evict(workspaceId);
        }
        if (cacheManager.getCache("board") != null) {
            cacheManager.getCache("board").evict(id);
        }
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "board", key = "#boardId")
    public BoardResponse getBoardCacheData(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .filter(b -> !b.isArchived() && !b.getWorkspace().isArchived())
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        return boardMapper.toResponse(board);
    }
}