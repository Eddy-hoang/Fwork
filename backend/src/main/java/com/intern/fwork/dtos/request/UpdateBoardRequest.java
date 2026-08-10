package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBoardRequest {

    @NotBlank(message = "Board title is required")
    @Size(max = 255, message = "Board title must not exceed 255 characters")
    private String title;

    private String description;

    private String color;
}
