package com.intern.fwork.dtos.response;

import com.intern.fwork.enums.TaskActivityAction;
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
public class TaskActivityResponse {
    private UUID id;
    private UUID taskId;
    private UserResponse actor;
    private TaskActivityAction action;
    private String detail;
    private LocalDateTime createdAt;
}
