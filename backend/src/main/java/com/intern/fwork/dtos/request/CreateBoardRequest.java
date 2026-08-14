package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateBoardRequest {

    @NotBlank(message = "Board title is required")
    @Size(max = 255, message = "Board title must not exceed 255 characters")
    private String title;

    private String description;

    private String color;

    private UUID workspaceId;

}
