package com.team202ok.demo.domain.user.entity;

import com.team202ok.demo.global.entity.BaseTimeEntity;
import com.team202ok.demo.domain.region.entity.Region;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false, unique = true)
    private String kakaoId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(name = "region_code", length = 50)
    private String regionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", referencedColumnName = "region_code",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_users_region_code"))
    private Region region;

    @Column(name = "is_notification_enabled", nullable = false)
    private Boolean isNotificationEnabled;

    @Builder
    private User(String kakaoId, String name, String profileImageUrl,
                 String regionCode, Boolean isNotificationEnabled) {
        this.kakaoId = kakaoId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.regionCode = regionCode;
        this.isNotificationEnabled = isNotificationEnabled != null ? isNotificationEnabled : true;
    }

    public void updateRegion(String regionCode) {
        this.regionCode = regionCode;
    }

    public void updateNotificationEnabled(boolean enabled) {
        this.isNotificationEnabled = enabled;
    }

    public void updateProfile(String name, String profileImageUrl) {
        if (name != null) {
            this.name = name;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}
