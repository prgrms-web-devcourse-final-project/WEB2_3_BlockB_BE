package com.example.earthtalk.domain.notification.dto.response;

import org.springframework.data.domain.Page;

public record NotificationListResponseWithUnreadCount(
        int unreadCount,
        Page<NotificationListResponse> notifications
) {
}
