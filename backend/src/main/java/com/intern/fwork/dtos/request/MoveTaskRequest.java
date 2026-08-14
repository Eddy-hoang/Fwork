package com.intern.fwork.dtos.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveTaskRequest {

    @NotNull(message = "Target column ID is required")
    @JsonAlias({"target_column_id", "columnId", "column_id"})
    private UUID targetColumnId;

    @NotNull(message = "Target position is required")
    @Min(value = 0, message = "Target position must be >= 0")
    @JsonAlias({"position", "target_position"})
    private Integer targetPosition;

    public void setPosition(Number pos) {
        if (pos != null) {
            this.targetPosition = pos.intValue();
        }
    }

    public void setTargetPosition(Number pos) {
        if (pos != null) {
            this.targetPosition = pos.intValue();
        }
    }
}
