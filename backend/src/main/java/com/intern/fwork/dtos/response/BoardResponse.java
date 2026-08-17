package com.intern.fwork.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {
    private UUID id;
    private String title;
    private String description;
    private String color;
    private UUID workspaceId;
    private Integer position;

    @JsonProperty("isArchived")
    private boolean isArchived;

    private UUID createdBy;
    private UUID updatedBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("task_count")
    private long taskCount;

    @JsonProperty("member_count")
    private long memberCount;

    @JsonProperty("is_owner")
    private boolean isOwner;
}
