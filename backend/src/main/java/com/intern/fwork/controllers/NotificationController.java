package com.intern.fwork.controllers;

import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.NotificationResponse;
import com.intern.fwork.dtos.response.NotificationUnreadCountResponse;
import com.intern.fwork.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getMyNotifications(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(notificationService.getMyNotifications(pageable));
    }

    @GetMapping("/unread-count")
    public ApiResponse<NotificationUnreadCountResponse> getUnreadCount() {
        return ApiResponse.success(notificationService.getUnreadCount());
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ApiResponse.success(null);
    }
}
