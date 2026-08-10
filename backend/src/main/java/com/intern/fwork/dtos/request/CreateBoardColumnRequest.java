package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBoardColumnRequest {

    @NotBlank(message = "Column name is required")
    private String name;

    @NotNull(message = "Position is required")
    private Integer position;

}
