package com.team202ok.demo.domain.ai.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "scan_results")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScanResult extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_scan_results_user"))
    private User user;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Builder
    private ScanResult(Long userId, String imageUrl) {
        this.userId = userId;
        this.imageUrl = imageUrl;
    }
}
