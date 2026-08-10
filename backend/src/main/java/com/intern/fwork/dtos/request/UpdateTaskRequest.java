package com.intern.fwork.dtos.request;

import com.intern.fwork.enums.Priority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title must not exceed 255 characters")
    private String title;

    private String description;

    private Priority priority;

    private LocalDateTime dueDate;

    @Min(value = 0, message = "Position must be >= 0")
    private Integer position;

}
