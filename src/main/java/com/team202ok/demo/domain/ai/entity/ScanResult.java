package com.team202ok.demo.domain.ai.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;

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

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Builder
    private ScanResult(Long userId, String imageUrl) {
        this.userId = userId;
        this.imageUrl = imageUrl;
    }
}
