package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWorkspaceRequest {

    @NotBlank(message = "Workspace name is required")
    @Size(max = 255, message = "Workspace name must not exceed 255 characters")
    private String name;

    private String description;
}
