package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.BoardResponse;
import com.intern.fwork.entities.Board;
import com.intern.fwork.entities.User;
import com.intern.fwork.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardMapper {

    @Autowired
    private SecurityUtils securityUtils;

    public BoardResponse toResponse(Board board) {
        if (board == null) {
            return null;
        }

        long taskCount = 0;
        if (board.getColumns() != null) {
            taskCount = board.getColumns().stream()
                    .filter(col -> col.getTasks() != null)
                    .flatMap(col -> col.getTasks().stream())
                    .filter(task -> !task.isArchived())
                    .count();
        }

        long memberCount = 0;
        if (board.getWorkspace() != null && board.getWorkspace().getMembers() != null) {
            memberCount = board.getWorkspace().getMembers().size();
        }

        boolean isOwner = false;
        try {
            User currentUser = securityUtils.getCurrentUser();
            if (currentUser != null && board.getCreatedBy() != null) {
                isOwner = board.getCreatedBy().getId().equals(currentUser.getId());
            }
        } catch (Exception e) {
            // Safe fallback if user is not authenticated (e.g., in test contexts)
        }

        return BoardResponse.builder()
                .id(board.getId())
                .title(board.getTitle())
                .description(board.getDescription())
                .color(board.getColor())
                .workspaceId(board.getWorkspace() != null ? board.getWorkspace().getId() : null)
                .position(board.getPosition())
                .isArchived(board.isArchived())
                .createdBy(board.getCreatedBy() != null ? board.getCreatedBy().getId() : null)
                .updatedBy(board.getUpdatedBy() != null ? board.getUpdatedBy().getId() : null)
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .taskCount(taskCount)
                .memberCount(memberCount)
                .isOwner(isOwner)
                .build();
    }
}