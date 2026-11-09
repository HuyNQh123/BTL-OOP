package com.smartenvelope.repository;

import com.smartenvelope.entity.Notification;
import com.smartenvelope.enums.NotificationStatus;
import com.smartenvelope.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDateTime;

/**
 * Repository thao tác dữ liệu với bảng notifications. Phụ trách bởi: Thành viên
 * 4 & Thành viên 5.
 */

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    long countByUserIdAndStatus(Long userId, NotificationStatus status);

    boolean existsByUserIdAndTypeAndCreatedAtBetween(Long userId, NotificationType type,
            LocalDateTime start, LocalDateTime end);
}
