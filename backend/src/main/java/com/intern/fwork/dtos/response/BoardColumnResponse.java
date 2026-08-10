package com.intern.fwork.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnResponse {

    private UUID id;

    private String name;

    private Integer position;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
