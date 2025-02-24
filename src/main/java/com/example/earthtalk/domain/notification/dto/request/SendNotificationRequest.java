package com.example.earthtalk.domain.notification.dto.request;

import com.example.earthtalk.domain.notification.entity.NotificationType;

public record SendNotificationRequest(Long userId, NotificationType notificationType, Long typeId) {

}
