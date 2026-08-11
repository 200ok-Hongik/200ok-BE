package com.team202ok.demo.domain.home.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class HomeRes {
    private HomeRes() {
    }

    public record TodaySchedule(Long calendarId, Long disposalDecisionId, Long categoryId,
                                String categoryName, LocalDateTime scheduledAt, boolean isCompleted) {
    }

    public record NotificationItem(Long notificationId, Long calendarId, String title, String content,
                                   LocalDateTime createdAt) {
    }

    public record Summary(List<TodaySchedule> todaySchedules, List<NotificationItem> recentNotifications) {
    }
}
