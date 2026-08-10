package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.WorkspaceResponse;
import com.intern.fwork.entities.Workspace;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

    public WorkspaceResponse toResponse(Workspace workspace) {
        if (workspace == null) {
            return null;
        }
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .description(workspace.getDescription())
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .createdBy(workspace.getCreatedBy() != null ? workspace.getCreatedBy().getId() : null)
                .updatedBy(workspace.getUpdatedBy() != null ? workspace.getUpdatedBy().getId() : null)
                .build();
    }
}
