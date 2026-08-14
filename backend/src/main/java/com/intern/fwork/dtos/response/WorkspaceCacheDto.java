package com.intern.fwork.dtos.response;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceCacheDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String slug;
    private String description;
    private long memberCount;
    private long boardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;
}
