package com.intern.fwork.services.impl;

import com.intern.fwork.entities.Comment;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.BoardColumn;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.WorkspaceMember;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.exceptions.BoardColumnNotFoundException;
import com.intern.fwork.exceptions.BoardNotFoundException;
import com.intern.fwork.exceptions.ForbiddenOperationException;
import com.intern.fwork.exceptions.ResourceNotFoundException;
import com.intern.fwork.exceptions.TaskNotFoundException;
import com.intern.fwork.exceptions.UserNotFoundException;
import com.intern.fwork.repositories.BoardColumnRepository;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.CommentRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.repositories.UserRepository;
import com.intern.fwork.repositories.WorkspaceMemberRepository;
import com.intern.fwork.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Override
    public WorkspaceRole getWorkspaceRole(UUID workspaceId, UUID userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .map(WorkspaceMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("You do not have access to this workspace"));
    }

    @Override
    public void checkReadWorkspace(UUID workspaceId, UUID userId) {
        boolean exists = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
        if (!exists) {
            throw new AccessDeniedException("You do not have access to this workspace");
        }
    }

    @Override
    public void checkEditWorkspace(UUID workspaceId, UUID userId) {
        WorkspaceRole role = getWorkspaceRole(workspaceId, userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can edit workspace settings");
        }
    }

    @Override
    public void checkDeleteWorkspace(UUID workspaceId, UUID userId) {
        WorkspaceRole role = getWorkspaceRole(workspaceId, userId);
        if (role != WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("Only the OWNER can delete the workspace");
        }
    }

    @Override
    public void checkManageMembers(UUID workspaceId, UUID userId) {
        WorkspaceRole role = getWorkspaceRole(workspaceId, userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can manage workspace members");
        }
    }

    @Override
    public void checkRemoveMember(UUID workspaceId, UUID currentUserId, UUID targetUserId) {
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in workspace"));

        if (targetMember.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("OWNER cannot be removed from the workspace");
        }

        WorkspaceMember currentMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Access Denied"));

        if (!currentUserId.equals(targetUserId)) {
            if (currentMember.getRole() != WorkspaceRole.OWNER && currentMember.getRole() != WorkspaceRole.ADMIN) {
                throw new ForbiddenOperationException("Only OWNER or ADMIN can manage workspace members");
            }
            if (targetMember.getRole() == WorkspaceRole.ADMIN && currentMember.getRole() == WorkspaceRole.ADMIN) {
                throw new ForbiddenOperationException("ADMIN cannot remove another ADMIN");
            }
        }
    }

    @Override
    public void checkWorkspaceAccess(UUID workspaceId, UUID userId) {
        checkReadWorkspace(workspaceId, userId);
    }

    @Override
    public void checkCreateBoard(UUID workspaceId, UUID userId) {
        WorkspaceRole role = getWorkspaceRole(workspaceId, userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can create boards");
        }
    }

    @Override
    public void checkUpdateBoard(UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        WorkspaceRole role = getWorkspaceRole(board.getWorkspace().getId(), userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can modify boards");
        }
    }

    @Override
    public void checkDeleteBoard(UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        WorkspaceRole role = getWorkspaceRole(board.getWorkspace().getId(), userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can delete boards");
        }
    }

    @Override
    public void checkCreateColumn(UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        WorkspaceRole role = getWorkspaceRole(board.getWorkspace().getId(), userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can create columns");
        }
    }

    @Override
    public void checkUpdateColumn(UUID columnId, UUID userId) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));
        WorkspaceRole role = getWorkspaceRole(column.getBoard().getWorkspace().getId(), userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can modify columns");
        }
    }

    @Override
    public void checkDeleteColumn(UUID columnId, UUID userId) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));
        WorkspaceRole role = getWorkspaceRole(column.getBoard().getWorkspace().getId(), userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can delete columns");
        }
    }

    @Override
    public void checkCreateTask(UUID columnId, UUID userId) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new BoardColumnNotFoundException("Column not found"));
        checkReadWorkspace(column.getBoard().getWorkspace().getId(), userId);
    }

    @Override
    public void checkUpdateTask(UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        checkReadWorkspace(task.getColumn().getBoard().getWorkspace().getId(), userId);
    }

    @Override
    public void checkDeleteTask(UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        WorkspaceRole role = getWorkspaceRole(task.getColumn().getBoard().getWorkspace().getId(), userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can delete tasks");
        }
    }

    @Override
    public void checkAssignTask(UUID taskId, UUID assigneeId, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        UUID workspaceId = task.getColumn().getBoard().getWorkspace().getId();

        // 1. Check current user has access to workspace (OWNER, ADMIN, or MEMBER)
        boolean existsCurrentUser = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
        if (!existsCurrentUser) {
            throw new AccessDeniedException("You do not have access to this workspace");
        }

        // 2. If assigneeId is provided, verify they exist and belong to the workspace
        if (assigneeId != null) {
            if (!userRepository.existsById(assigneeId)) {
                throw new UserNotFoundException("Assignee not found");
            }
            if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, assigneeId)) {
                throw new AccessDeniedException("Assignee must be a member of the workspace");
            }
        }
    }

    @Override
    public void checkManageLabels(UUID boardId, UUID userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found"));
        WorkspaceRole role = getWorkspaceRole(board.getWorkspace().getId(), userId);
        if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
            throw new ForbiddenOperationException("Only OWNER or ADMIN can manage board labels");
        }
    }

    @Override
    public void checkCreateComment(UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));
        checkReadWorkspace(task.getColumn().getBoard().getWorkspace().getId(), userId);
    }

    @Override
    public void checkUpdateComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getCreatedBy().getId().equals(userId)) {
            throw new ForbiddenOperationException("Only the author can edit this comment");
        }
    }

    @Override
    public void checkDeleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        UUID workspaceId = comment.getTask().getColumn().getBoard().getWorkspace().getId();
        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
        if (!isMember) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have access to this workspace");
        }

        boolean isAuthor = comment.getCreatedBy().getId().equals(userId);
        if (!isAuthor) {
            WorkspaceRole role = getWorkspaceRole(workspaceId, userId);
            if (role != WorkspaceRole.OWNER && role != WorkspaceRole.ADMIN) {
                throw new ForbiddenOperationException("You do not have permission to delete this comment");
            }
        }
    }
}
