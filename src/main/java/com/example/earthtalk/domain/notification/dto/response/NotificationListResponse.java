package com.example.earthtalk.domain.notification.dto.response;

import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.notification.entity.NotificationType;
import com.example.earthtalk.domain.notification.entity.StatusType;

import java.time.LocalDateTime;

public record NotificationListResponse(
        NotificationType notificationType,
        Long typeId,
        String content,
        StatusType statusType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static NotificationListResponse from(Notification notification) {
        return new NotificationListResponse(
                notification.getNotificationType(),
                notification.getNotificationTypeId(),
                notification.getContent(),
                notification.getStatusType(),
                notification.getCreatedAt(),
                notification.getUpdatedAt()
        );
    }
}
