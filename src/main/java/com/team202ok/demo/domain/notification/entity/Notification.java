package com.team202ok.demo.domain.notification.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "notifications", uniqueConstraints =
        @UniqueConstraint(name = "uk_notifications_calendar_id", columnNames = "calendar_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseCreatedAtEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(name = "calendar_id")
    private Long calendarId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @Builder
    private Notification(Long userId, Long calendarId, String title, String content, LocalDateTime scheduledAt, LocalDateTime sentAt) {
        this.userId = userId;
        this.calendarId = calendarId;
        this.title = title;
        this.content = content;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
    }

    public void markSent(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
