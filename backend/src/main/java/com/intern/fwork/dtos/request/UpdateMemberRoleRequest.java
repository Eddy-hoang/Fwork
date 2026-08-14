package com.intern.fwork.dtos.request;

import com.intern.fwork.enums.WorkspaceRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateMemberRoleRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private WorkspaceRole role;
}
