package com.intern.fwork.services;

import com.intern.fwork.dtos.response.NotificationResponse;
import com.intern.fwork.dtos.response.NotificationUnreadCountResponse;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    void createNotification(User recipient, User actor, NotificationType type, String title, 
                            String message, String referenceType, UUID referenceId);

    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    NotificationUnreadCountResponse getUnreadCount();

    void markAsRead(UUID id);

    void markAllAsRead();

    void delete(UUID id);
}
