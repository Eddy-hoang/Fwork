package com.intern.fwork.dtos.response;

import com.intern.fwork.enums.WorkspaceRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private long memberCount;
    private long boardCount;
    private WorkspaceRole currentUserRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
