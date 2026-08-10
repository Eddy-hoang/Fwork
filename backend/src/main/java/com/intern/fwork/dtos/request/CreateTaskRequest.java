package com.intern.fwork.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.intern.fwork.enums.Priority;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title must not exceed 255 characters")
    private String title;

    private String description;

    @NotNull(message = "Task priority is required")
    private Priority priority = Priority.MEDIUM;

    private LocalDateTime dueDate;

    @NotNull(message = "Task position is required")
    @Min(value = 0, message = "Position must be >= 0")
    private Integer position;

}
