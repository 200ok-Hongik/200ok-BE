package com.team202ok.demo.domain.notification.service;

import java.time.LocalDate;

public interface NotificationService {
    int createDisposalNotifications(LocalDate date);
    int sendTestPush(Long userId, String itemName);
}
