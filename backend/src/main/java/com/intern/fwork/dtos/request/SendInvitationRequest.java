package com.intern.fwork.dtos.request;

import com.intern.fwork.enums.WorkspaceRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendInvitationRequest {

    @NotBlank
    @Email
    private String email;

    @NotNull
    private WorkspaceRole role;
}
