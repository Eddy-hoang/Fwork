package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    private String name;

    private String description;
}
