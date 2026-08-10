package com.intern.fwork.dtos.request;

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
    private Integer targetPosition;

}
