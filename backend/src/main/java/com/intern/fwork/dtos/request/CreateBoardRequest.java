package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateBoardRequest {

    @NotBlank
    private String title;

    private String description;

    private String color;

    @NotNull
    private UUID workspaceId;

}
