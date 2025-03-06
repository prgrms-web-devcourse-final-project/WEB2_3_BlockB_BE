package com.example.earthtalk.domain.notification.repository;

import com.example.earthtalk.domain.notification.entity.Notification;
import com.example.earthtalk.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM notifications n WHERE n.user = :user ORDER BY n.id DESC")
    Slice<Notification> getNotifications(@Param("user") User user, Pageable pageable);

    @Modifying
    @Query("UPDATE notifications n SET n.statusType = 'READ' WHERE n.user = :user")
    void markAllAsReadByUserId(@Param("user") User user);

    void deleteAllByUserId(Long id);
}
