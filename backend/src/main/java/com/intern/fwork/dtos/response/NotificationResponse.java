package com.intern.fwork.dtos.response;

import com.intern.fwork.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private UserResponse actor;
    private NotificationType type;
    private String title;
    private String message;
    private String referenceType;
    private UUID referenceId;
    private boolean isRead;
    private LocalDateTime createdAt;
}
