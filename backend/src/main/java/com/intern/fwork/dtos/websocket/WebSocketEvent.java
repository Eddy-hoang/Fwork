package com.intern.fwork.dtos.websocket;

import com.intern.fwork.dtos.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent {

    private String eventId;
    private String eventType;
    @Builder.Default
    private Integer version = 1;
    private UUID boardId;
    private UserResponse actor;
    private TargetRef target;
    private Map<String, Object> data;
    private LocalDateTime occurredAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TargetRef {
        private String type;
        private UUID id;
    }
}
