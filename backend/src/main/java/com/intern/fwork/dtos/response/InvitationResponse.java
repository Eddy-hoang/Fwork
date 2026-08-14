package com.intern.fwork.dtos.response;

import com.intern.fwork.enums.InvitationStatus;
import com.intern.fwork.enums.WorkspaceRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InvitationResponse {
    private UUID id;
    private UUID workspaceId;
    private String workspaceName;
    private String email;
    private WorkspaceRole role;
    private InvitationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
