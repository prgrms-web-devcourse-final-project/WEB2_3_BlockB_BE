package com.example.earthtalk.domain.notification.dto.response;

import com.example.earthtalk.domain.notification.entity.Notification;

public record NotificationResponse(
        Long id,
        String type,
        Long senderId,
        String content,
        String status
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType().name(),
                notification.getNotificationTypeId(),
                notification.getContent(),
                notification.getStatusType().name()
        );
    }
}
