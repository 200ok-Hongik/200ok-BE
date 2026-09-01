package com.team202ok.demo.domain.notification.entity;

import com.team202ok.demo.global.entity.BaseTimeEntity;
import com.team202ok.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "push_devices", uniqueConstraints =
        @UniqueConstraint(name = "uk_push_devices_token", columnNames = "token"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushDevice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_push_devices_user"))
    private User user;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(name = "device_type", nullable = false, length = 20)
    private String deviceType;

    @Builder
    private PushDevice(Long userId, String token, String deviceType) {
        this.userId = userId;
        this.token = token;
        this.deviceType = deviceType;
    }

    public void registerTo(Long userId, String deviceType) {
        this.userId = userId;
        this.deviceType = deviceType;
    }
}
