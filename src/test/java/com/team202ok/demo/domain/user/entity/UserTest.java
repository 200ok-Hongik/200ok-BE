package com.team202ok.demo.domain.user.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void partialProfileUpdateKeepsOmittedFields() {
        User user = User.builder()
                .kakaoId("kakao-user")
                .name("기존 이름")
                .profileImageUrl("https://example.com/old.jpg")
                .build();

        user.updateProfile("새 이름", null);

        assertThat(user.getName()).isEqualTo("새 이름");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/old.jpg");
    }

    @Test
    void notificationCanBeDisabledAndEnabled() {
        User user = User.builder().kakaoId("notification-user").build();

        assertThat(user.getIsNotificationEnabled()).isTrue();
        user.updateNotificationEnabled(false);
        assertThat(user.getIsNotificationEnabled()).isFalse();
        user.updateNotificationEnabled(true);
        assertThat(user.getIsNotificationEnabled()).isTrue();
    }
}
