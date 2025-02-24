package com.example.earthtalk.domain.notification.repository;

import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM notifications n WHERE n.user = :user")
    List<Notification> getNotifications(@Param("user") User user);
}
