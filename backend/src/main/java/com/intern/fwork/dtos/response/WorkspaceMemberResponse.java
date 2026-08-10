package com.intern.fwork.dtos.response;

import com.intern.fwork.enums.WorkspaceRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String avatar;
    private WorkspaceRole role;
    private LocalDateTime joinedAt;
}
