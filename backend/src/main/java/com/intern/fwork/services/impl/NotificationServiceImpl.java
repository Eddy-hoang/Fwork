package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.response.NotificationResponse;
import com.intern.fwork.dtos.response.NotificationUnreadCountResponse;
import com.intern.fwork.entities.Notification;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.NotificationType;
import com.intern.fwork.exceptions.ResourceNotFoundException;
import com.intern.fwork.mappers.NotificationMapper;
import com.intern.fwork.repositories.NotificationRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SecurityUtils securityUtils;

    @Override
    public void createNotification(User recipient, User actor, NotificationType type, String title, 
                            String message, String referenceType, UUID referenceId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .title(title)
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        User currentUser = securityUtils.getCurrentUser();
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse getUnreadCount() {
        User currentUser = securityUtils.getCurrentUser();
        long count = notificationRepository.countUnreadByRecipientId(currentUser.getId());
        return new NotificationUnreadCountResponse(count);
    }

    @Override
    public void markAsRead(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to modify this notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead() {
        User currentUser = securityUtils.getCurrentUser();
        notificationRepository.markAllAsReadForRecipient(currentUser.getId());
    }

    @Override
    public void delete(UUID id) {
        User currentUser = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this notification");
        }
        notificationRepository.delete(notification);
    }
}
