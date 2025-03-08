package com.example.earthtalk.domain.notification.repository;

import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM notifications n WHERE n.user = :user ORDER BY n.id DESC")
    Page<Notification> getNotifications(@Param("user") User user, Pageable pageable);

    @Modifying
    @Query("UPDATE notifications n SET n.statusType = 'READ' WHERE n.user = :user")
    void markAllAsReadByUserId(@Param("user") User user);

    @Query("SELECT COUNT(*) FROM notifications n WHERE n.user = :user AND n.statusType = 'UNREAD'")
    int getCountUnread(@Param("user") User user);

    void deleteAllByUserId(Long id);
}
