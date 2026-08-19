package com.team202ok.demo.domain.notification.service;

import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import com.team202ok.demo.domain.calendar.repository.UserCalendarRepository;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import com.team202ok.demo.domain.notification.entity.Notification;
import com.team202ok.demo.domain.notification.repository.NotificationRepository;
import com.team202ok.demo.domain.notification.push.PushNotificationSender;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {
    @Mock UserCalendarRepository userCalendarRepository;
    @Mock NotificationRepository notificationRepository;
    @Mock TrashCategoryRepository trashCategoryRepository;
    @Mock UserRepository userRepository;
    @Mock PushNotificationSender pushNotificationSender;
    @InjectMocks NotificationServiceImpl service;

    @Test
    void createsNotificationContainingTheExactScannedCategory() {
        LocalDate date = LocalDate.of(2026, 8, 19);
        UserCalendar calendar = UserCalendar.builder().userId(7L).disposalDecisionId(11L)
                .trashCategoryId(3L).scheduledAt(date.atTime(18, 0)).build();
        User user = User.builder().kakaoId("kakao-7").isNotificationEnabled(true).build();
        TrashCategory category = TrashCategory.builder().code("PET").name("투명 페트병").build();
        when(userCalendarRepository.findByScheduledAtGreaterThanEqualAndScheduledAtLessThanAndIsCompletedFalse(any(), any()))
                .thenReturn(List.of(calendar));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(trashCategoryRepository.findById(3L)).thenReturn(Optional.of(category));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pushNotificationSender.send(anyLong(), any())).thenReturn(1);

        assertThat(service.createDisposalNotifications(date)).isEqualTo(1);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getContent()).isEqualTo("오늘은 전에 스캔한 '투명 페트병' 배출하는 날이에요!");
        assertThat(captor.getValue().getSentAt()).isNotNull();
    }

    @Test
    void doesNotCreateDuplicateOrDisabledNotification() {
        LocalDate date = LocalDate.of(2026, 8, 19);
        UserCalendar duplicate = UserCalendar.builder().userId(7L).disposalDecisionId(11L)
                .trashCategoryId(3L).scheduledAt(date.atTime(18, 0)).build();
        when(userCalendarRepository.findByScheduledAtGreaterThanEqualAndScheduledAtLessThanAndIsCompletedFalse(any(), any()))
                .thenReturn(List.of(duplicate));
        when(notificationRepository.existsByCalendarId(duplicate.getId())).thenReturn(true);

        assertThat(service.createDisposalNotifications(date)).isZero();
        verify(notificationRepository, never()).save(any());
        verifyNoInteractions(userRepository, trashCategoryRepository);
    }
}
