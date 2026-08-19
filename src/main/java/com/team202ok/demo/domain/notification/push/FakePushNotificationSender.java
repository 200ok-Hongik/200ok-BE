package com.team202ok.demo.domain.notification.push;

import com.team202ok.demo.domain.notification.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.push.provider", havingValue = "fake", matchIfMissing = true)
public class FakePushNotificationSender implements PushNotificationSender {
    private final PushDeviceRepository pushDeviceRepository;

    @Override
    public int send(Long userId, PushMessage message) {
        var devices = pushDeviceRepository.findByUserId(userId);
        devices.forEach(device -> log.info(
                "[FAKE PUSH] userId={}, deviceType={}, title={}, body={}, calendarId={}, disposalDecisionId={}, type={}",
                userId, device.getDeviceType(), message.title(), message.body(), message.calendarId(),
                message.disposalDecisionId(), message.type()));
        return devices.size();
    }
}
