package com.intern.fwork.mappers;

import com.intern.fwork.dtos.response.LabelResponse;
import com.intern.fwork.entities.Label;
import org.springframework.stereotype.Component;

@Component
public class LabelMapper {

    public LabelResponse toResponse(Label label) {
        if (label == null) {
            return null;
        }
        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .color(label.getColor())
                .boardId(label.getBoard() != null ? label.getBoard().getId() : null)
                .build();
    }
}
