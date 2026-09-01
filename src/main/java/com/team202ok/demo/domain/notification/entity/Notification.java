package com.team202ok.demo.domain.notification.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import com.team202ok.demo.domain.user.entity.User;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_notifications_user"))
    private User user;

    @Column(name = "calendar_id")
    private Long calendarId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_notifications_calendar"))
    private UserCalendar calendar;

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
