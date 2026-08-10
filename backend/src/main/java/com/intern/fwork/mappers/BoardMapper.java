package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.BoardResponse;
import com.intern.fwork.entities.Board;
import org.springframework.stereotype.Component;

@Component
public class BoardMapper {

    public BoardResponse toResponse(Board board) {
        if (board == null) {
            return null;
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
                .build();
    }
}