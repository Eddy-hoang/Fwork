package com.intern.fwork.dtos.websocket;

import com.intern.fwork.dtos.response.UserResponse;
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
public class WebSocketEvent {

    private String type;
    private UUID boardId;
    private UserResponse actor;
    private Object payload;
    private LocalDateTime occurredAt;
}
