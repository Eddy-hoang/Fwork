package com.intern.fwork.dtos.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intern.fwork.enums.Priority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title must not exceed 255 characters")
    private String title;

    private String description;

    private Priority priority;

    @JsonAlias({"due_date", "dueDate"})
    private LocalDateTime dueDate;

    @Min(value = 0, message = "Position must be >= 0")
    private Integer position;

    @JsonProperty("isCompleted")
    private Boolean isCompleted;

    public void setDueDate(Object value) {
        if (value == null) {
            this.dueDate = null;
            return;
        }
        if (value instanceof LocalDateTime ldt) {
            this.dueDate = ldt;
        } else if (value instanceof String str) {
            str = str.trim();
            if (str.isEmpty()) {
                this.dueDate = null;
            } else if (str.length() == 10) {
                this.dueDate = LocalDate.parse(str).atTime(23, 59, 59);
            } else {
                try {
                    this.dueDate = LocalDateTime.parse(str);
                } catch (Exception e) {
                    try {
                        this.dueDate = LocalDate.parse(str.substring(0, 10)).atTime(23, 59, 59);
                    } catch (Exception ex) {
                        this.dueDate = null;
                    }
                }
            }
        }
    }
}
