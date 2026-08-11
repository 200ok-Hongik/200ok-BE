package com.team202ok.demo.domain.home.service;

import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import com.team202ok.demo.domain.calendar.repository.UserCalendarRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import com.team202ok.demo.domain.home.dto.HomeRes;
import com.team202ok.demo.domain.notification.entity.Notification;
import com.team202ok.demo.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {
    private final UserCalendarRepository userCalendarRepository;
    private final NotificationRepository notificationRepository;
    private final TrashCategoryRepository trashCategoryRepository;

    @Override
    public HomeRes.Summary getSummary(Long userId) {
        return new HomeRes.Summary(getTodaySchedules(userId), getRecentNotifications(userId));
    }

    @Override
    public List<HomeRes.TodaySchedule> getTodaySchedules(Long userId) {
        LocalDate today = LocalDate.now();
        return userCalendarRepository.findByUserIdAndScheduledAtBetweenOrderByScheduledAtAsc(
                        userId, today.atStartOfDay(), today.plusDays(1).atStartOfDay())
                .stream()
                .map(this::toTodaySchedule)
                .toList();
    }

    @Override
    public List<HomeRes.NotificationItem> getRecentNotifications(Long userId) {
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toNotification)
                .toList();
    }

    private HomeRes.TodaySchedule toTodaySchedule(UserCalendar calendar) {
        String categoryName = trashCategoryRepository.findById(calendar.getTrashCategoryId())
                .map(category -> category.getName())
                .orElse("알 수 없는 품목");
        return new HomeRes.TodaySchedule(calendar.getId(), calendar.getDisposalDecisionId(), calendar.getTrashCategoryId(),
                categoryName, calendar.getScheduledAt(), calendar.getIsCompleted());
    }

    private HomeRes.NotificationItem toNotification(Notification notification) {
        return new HomeRes.NotificationItem(notification.getId(), notification.getCalendarId(), notification.getTitle(),
                notification.getContent(), notification.getCreatedAt());
    }
}
