package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.BoardColumnResponse;
import com.intern.fwork.entities.BoardColumn;
import org.springframework.stereotype.Component;

@Component
public class BoardColumnMapper {

    public BoardColumnResponse toResponse(BoardColumn column) {
        if (column == null) {
            return null;
        }
        return BoardColumnResponse.builder()
                .id(column.getId())
                .name(column.getName())
                .position(column.getPosition())
                .createdAt(column.getCreatedAt())
                .updatedAt(column.getUpdatedAt())
                .build();
    }
}
