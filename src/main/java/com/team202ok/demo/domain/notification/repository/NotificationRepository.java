package com.team202ok.demo.domain.notification.repository;

import com.team202ok.demo.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByCalendarId(Long calendarId);
}
