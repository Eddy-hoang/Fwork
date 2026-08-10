package com.intern.fwork.dtos.request;

import com.intern.fwork.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Task priority is required")
    private Priority priority = Priority.MEDIUM;

    private LocalDateTime dueDate;

    @NotNull(message = "Task position is required")
    private Integer position;

}
