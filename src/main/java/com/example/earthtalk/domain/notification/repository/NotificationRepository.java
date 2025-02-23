package com.example.earthtalk.domain.notification.repository;

import com.example.earthtalk.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
