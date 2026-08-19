package com.team202ok.demo.domain.notification.push;

public interface PushNotificationSender {
    int send(Long userId, PushMessage message);
}
