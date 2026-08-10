package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBoardColumnRequest {

    @NotBlank(message = "Column name is required")
    @Size(max = 255, message = "Column name must not exceed 255 characters")
    private String name;

    @NotNull(message = "Position is required")
    @Min(value = 0, message = "Position must be >= 0")
    private Integer position;

}
