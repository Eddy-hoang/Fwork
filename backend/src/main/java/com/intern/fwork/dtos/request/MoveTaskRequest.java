package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MoveTaskRequest {

    @NotNull(message = "Target column ID is required")
    private UUID targetColumnId;

    @NotNull(message = "Target position is required")
    @Min(value = 0, message = "Target position must be >= 0")
    private Integer targetPosition;

}
