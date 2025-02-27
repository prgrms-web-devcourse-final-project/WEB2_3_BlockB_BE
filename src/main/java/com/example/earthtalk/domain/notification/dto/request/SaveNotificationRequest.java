package com.example.earthtalk.domain.notification.dto.request;

import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.notification.entity.NotificationType;
import com.example.earthtalk.domain.notification.entity.StatusType;
import com.example.earthtalk.domain.user.entity.User;

public record SaveNotificationRequest (Long userId, NotificationType notificationType, Long typeId, String content){

    public Notification toEntity(User user) {
        return Notification.builder()
                .user(user)
                .notificationType(notificationType)
                .notificationTypeId(typeId)
                .content(content)
                .statusType(StatusType.UNREAD)
                .build();
    }
}
