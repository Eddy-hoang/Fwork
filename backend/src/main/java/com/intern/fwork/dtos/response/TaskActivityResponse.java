package com.intern.fwork.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskActivityResponse {

    private UUID id;
    private UUID boardId;
    private UUID actorId;
    private String actorName;
    private String actorAvatar;
    private String actionType;
    private String targetType;
    private UUID targetId;
    private String description;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
