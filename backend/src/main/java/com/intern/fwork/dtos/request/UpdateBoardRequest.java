package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBoardRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String color;
}
