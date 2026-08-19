package com.team202ok.demo.domain.notification.service;

import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import com.team202ok.demo.domain.calendar.repository.UserCalendarRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import com.team202ok.demo.domain.notification.entity.Notification;
import com.team202ok.demo.domain.notification.repository.NotificationRepository;
import com.team202ok.demo.domain.notification.push.PushMessage;
import com.team202ok.demo.domain.notification.push.PushNotificationSender;
import com.team202ok.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final UserCalendarRepository userCalendarRepository;
    private final NotificationRepository notificationRepository;
    private final TrashCategoryRepository trashCategoryRepository;
    private final UserRepository userRepository;
    private final PushNotificationSender pushNotificationSender;

    @Value("${notification.time-zone:Asia/Seoul}")
    private String timeZone;

    /**
     * 매일 아침, 지역 규칙으로 계산되어 캘린더에 저장된 오늘의 배출 품목을 알림으로 만든다.
     * 서버가 여러 대여도 calendarId 기준 중복 검사를 통해 같은 일정의 알림은 한 번만 생성한다.
     */
    @Scheduled(cron = "${notification.disposal.cron:0 0 9 * * *}", zone = "${notification.time-zone:Asia/Seoul}")
    @Transactional
    public void createTodayDisposalNotifications() {
        createDisposalNotifications(LocalDate.now(notificationZone()));
    }

    @Override
    @Transactional
    public int createDisposalNotifications(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        int createdCount = 0;

        for (UserCalendar calendar : userCalendarRepository
                .findByScheduledAtGreaterThanEqualAndScheduledAtLessThanAndIsCompletedFalse(start, end)) {
            if (notificationRepository.existsByCalendarId(calendar.getId())) {
                continue;
            }
            boolean enabled = userRepository.findById(calendar.getUserId())
                    .map(user -> Boolean.TRUE.equals(user.getIsNotificationEnabled()))
                    .orElse(false);
            if (!enabled) {
                continue;
            }

            String itemName = trashCategoryRepository.findById(calendar.getTrashCategoryId())
                    .map(category -> category.getName())
                    .orElse(null);
            if (itemName == null) {
                continue;
            }

            String title = "오늘은 분리배출하는 날이에요!";
            String content = "오늘은 전에 스캔한 '" + itemName + "' 배출하는 날이에요!";
            Notification notification = notificationRepository.save(Notification.builder()
                    .userId(calendar.getUserId())
                    .calendarId(calendar.getId())
                    .title(title)
                    .content(content)
                    .scheduledAt(calendar.getScheduledAt())
                    .build());
            int sentCount = pushNotificationSender.send(calendar.getUserId(), new PushMessage(
                    title, content, calendar.getId(), calendar.getDisposalDecisionId(), "DISPOSAL_REMINDER"));
            if (sentCount > 0) {
                notification.markSent(LocalDateTime.now(notificationZone()));
            }
            createdCount++;
        }
        return createdCount;
    }

    @Override
    public int sendTestPush(Long userId, String itemName) {
        if (!userRepository.existsById(userId)) {
            throw new com.team202ok.demo.global.exception.custom.ProjectException(
                    com.team202ok.demo.global.exception.code.GeneralErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        String normalizedName = itemName == null || itemName.isBlank() ? "투명 페트병" : itemName.trim();
        return pushNotificationSender.send(userId, new PushMessage(
                "오늘은 분리배출하는 날이에요!",
                "오늘은 전에 스캔한 '" + normalizedName + "' 배출하는 날이에요!",
                null, null, "DISPOSAL_REMINDER_TEST"));
    }

    private ZoneId notificationZone() {
        return ZoneId.of(timeZone == null || timeZone.isBlank() ? "Asia/Seoul" : timeZone);
    }
}
