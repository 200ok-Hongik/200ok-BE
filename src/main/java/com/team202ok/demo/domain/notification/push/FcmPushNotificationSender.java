package com.team202ok.demo.domain.notification.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.team202ok.demo.domain.notification.entity.PushDevice;
import com.team202ok.demo.domain.notification.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.push.provider", havingValue = "fcm")
class FcmPushNotificationSender implements PushNotificationSender {
    private final FirebaseMessaging firebaseMessaging;
    private final PushDeviceRepository pushDeviceRepository;

    @Override
    public int send(Long userId, PushMessage message) {
        Notification notification = Notification.builder()
                .setTitle(message.title())
                .setBody(message.body())
                .build();
        int sentCount = 0;

        for (PushDevice device : pushDeviceRepository.findByUserId(userId)) {
            Message.Builder request = Message.builder()
                    .setToken(device.getToken())
                    .setNotification(notification)
                    .putData("type", message.type());
            if (message.calendarId() != null) {
                request.putData("calendarId", message.calendarId().toString());
            }
            if (message.disposalDecisionId() != null) {
                request.putData("disposalDecisionId", message.disposalDecisionId().toString());
            }

            try {
                firebaseMessaging.send(request.build());
                sentCount++;
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED) {
                    pushDeviceRepository.deleteByUserIdAndToken(userId, device.getToken());
                }
                log.warn("FCM 발송 실패. userId={}, deviceId={}, code={}, messagingCode={}",
                        userId, device.getId(), e.getErrorCode(), code);
            }
        }
        return sentCount;
    }
}
