package com.intern.fwork.services;

import com.intern.fwork.entities.*;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.exceptions.ForbiddenOperationException;
import com.intern.fwork.repositories.*;
import com.intern.fwork.services.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceTest {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardColumnRepository boardColumnRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private UUID userId;
    private UUID workspaceId;
    private User user;
    private Workspace workspace;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        workspaceId = UUID.randomUUID();
        user = User.builder().id(userId).email("test@fwork.com").build();
        workspace = Workspace.builder().id(workspaceId).name("Test Workspace").build();
    }

    @Test
    public void testCheckWorkspaceAccess_Success() {
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(true);

        assertDoesNotThrow(() -> permissionService.checkWorkspaceAccess(workspaceId, userId));
    }

    @Test
    public void testCheckWorkspaceAccess_Forbidden() {
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(false);

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                permissionService.checkWorkspaceAccess(workspaceId, userId));
    }

    @Test
    public void testCheckCreateBoard_OwnerSuccess() {
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(member));

        assertDoesNotThrow(() -> permissionService.checkCreateBoard(workspaceId, userId));
    }

    @Test
    public void testCheckCreateBoard_AdminSuccess() {
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.ADMIN)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(member));

        assertDoesNotThrow(() -> permissionService.checkCreateBoard(workspaceId, userId));
    }

    @Test
    public void testCheckCreateBoard_MemberForbidden() {
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(member));

        assertThrows(ForbiddenOperationException.class, () ->
                permissionService.checkCreateBoard(workspaceId, userId));
    }

    @Test
    public void testCheckRemoveMember_SelfLeaveSuccess() {
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(member));

        // Operator is removing themselves
        assertDoesNotThrow(() -> permissionService.checkRemoveMember(workspaceId, userId, userId));
    }

    @Test
    public void testCheckRemoveMember_OwnerRemoveAdminSuccess() {
        UUID targetUserId = UUID.randomUUID();

        WorkspaceMember operatorMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        WorkspaceMember targetMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(User.builder().id(targetUserId).build())
                .role(WorkspaceRole.ADMIN)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(operatorMember));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId))
                .thenReturn(Optional.of(targetMember));

        assertDoesNotThrow(() -> permissionService.checkRemoveMember(workspaceId, userId, targetUserId));
    }

    @Test
    public void testCheckRemoveMember_AdminRemoveMemberSuccess() {
        UUID targetUserId = UUID.randomUUID();

        WorkspaceMember operatorMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.ADMIN)
                .build();

        WorkspaceMember targetMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(User.builder().id(targetUserId).build())
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(operatorMember));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId))
                .thenReturn(Optional.of(targetMember));

        assertDoesNotThrow(() -> permissionService.checkRemoveMember(workspaceId, userId, targetUserId));
    }

    @Test
    public void testCheckRemoveMember_AdminRemoveOwnerForbidden() {
        UUID targetUserId = UUID.randomUUID();

        WorkspaceMember targetMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(User.builder().id(targetUserId).build())
                .role(WorkspaceRole.OWNER)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId))
                .thenReturn(Optional.of(targetMember));

        assertThrows(ForbiddenOperationException.class, () ->
                permissionService.checkRemoveMember(workspaceId, userId, targetUserId));
    }

    @Test
    public void testCheckRemoveMember_MemberRemoveOtherForbidden() {
        UUID targetUserId = UUID.randomUUID();

        WorkspaceMember operatorMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.MEMBER)
                .build();

        WorkspaceMember targetMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(User.builder().id(targetUserId).build())
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                .thenReturn(Optional.of(operatorMember));
        when(workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId))
                .thenReturn(Optional.of(targetMember));

        assertThrows(ForbiddenOperationException.class, () ->
                permissionService.checkRemoveMember(workspaceId, userId, targetUserId));
    }
}
