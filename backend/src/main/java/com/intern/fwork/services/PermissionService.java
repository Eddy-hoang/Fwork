package com.intern.fwork.services;

import com.intern.fwork.enums.WorkspaceRole;
import java.util.UUID;

public interface PermissionService {
    
    WorkspaceRole getWorkspaceRole(UUID workspaceId, UUID userId);
    
    void checkReadWorkspace(UUID workspaceId, UUID userId);
    
    void checkEditWorkspace(UUID workspaceId, UUID userId);
    
    void checkDeleteWorkspace(UUID workspaceId, UUID userId);
    
    void checkManageMembers(UUID workspaceId, UUID userId);

    void checkWorkspaceAccess(UUID workspaceId, UUID userId);
    
    void checkCreateBoard(UUID workspaceId, UUID userId);
    
    void checkUpdateBoard(UUID boardId, UUID userId);
    
    void checkDeleteBoard(UUID boardId, UUID userId);

    void checkCreateColumn(UUID boardId, UUID userId);
    
    void checkUpdateColumn(UUID columnId, UUID userId);
    
    void checkDeleteColumn(UUID columnId, UUID userId);

    void checkCreateTask(UUID columnId, UUID userId);
    
    void checkUpdateTask(UUID taskId, UUID userId);
    
    void checkDeleteTask(UUID taskId, UUID userId);

    void checkAssignTask(UUID taskId, UUID assigneeId, UUID userId);

    void checkManageLabels(UUID boardId, UUID userId);

    void checkCreateComment(UUID taskId, UUID userId);

    void checkUpdateComment(UUID commentId, UUID userId);

    void checkDeleteComment(UUID commentId, UUID userId);
}
