package com.example.earthtalk.domain.notification.repository;

import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM notifications n WHERE n.user = :user ORDER BY n.id DESC LIMIT 10")
    List<Notification> getNotifications(@Param("user") User user);

    @Modifying
    @Query("UPDATE notifications n SET n.statusType = 'READ' WHERE n.user = :userId")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    void deleteAllByUserId(Long id);
}
