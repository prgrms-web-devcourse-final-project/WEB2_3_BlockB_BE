package com.example.earthtalk.domain.notification.dto.request;

import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.notification.entity.NotificationType;
import com.example.earthtalk.domain.notification.entity.StatusType;
import com.example.earthtalk.domain.user.entity.User;

public record SaveNotificationRequest (User user, NotificationType notificationType, Long typeId, String content){

    public Notification toEntity() {
        return Notification.builder()
                .user(user)
                .notificationType(notificationType)
                .notificationTypeId(typeId)
                .content(content)
                .statusType(StatusType.UNREAD)
                .build();
    }
}
