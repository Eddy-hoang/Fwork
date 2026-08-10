package com.intern.fwork.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intern.fwork.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private UUID id;

    private String title;

    private String description;

    private Integer position;

    private Priority priority;

    private LocalDateTime dueDate;

    private UUID columnId;

    private UUID createdBy;

    private UUID updatedBy;

    private UUID assigneeId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @JsonProperty("isArchived")
    private boolean isArchived;

}
