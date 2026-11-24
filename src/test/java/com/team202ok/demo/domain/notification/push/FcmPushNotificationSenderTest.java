package com.team202ok.demo.domain.notification.push;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.team202ok.demo.domain.notification.entity.PushDevice;
import com.team202ok.demo.domain.notification.repository.PushDeviceRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FcmPushNotificationSenderTest {

    @Test
    void removesUnregisteredTokenAndContinuesSendingToOtherDevices() throws FirebaseMessagingException {
        FirebaseMessaging firebaseMessaging = mock(FirebaseMessaging.class);
        PushDeviceRepository repository = mock(PushDeviceRepository.class);
        PushDevice expired = PushDevice.builder().userId(7L).token("expired").deviceType("WEB").build();
        PushDevice active = PushDevice.builder().userId(7L).token("active").deviceType("ANDROID").build();
        FirebaseMessagingException unregistered = mock(FirebaseMessagingException.class);
        when(unregistered.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        when(repository.findByUserId(7L)).thenReturn(List.of(expired, active));
        when(firebaseMessaging.send(any(Message.class))).thenThrow(unregistered).thenReturn("message-id");

        int accepted = new FcmPushNotificationSender(firebaseMessaging, repository)
                .send(7L, new PushMessage("배출 알림", "오늘 배출하세요", 11L, 12L, "DISPOSAL_REMINDER"));

        assertThat(accepted).isEqualTo(1);
        verify(repository).deleteByUserIdAndToken(7L, "expired");
    }
}
