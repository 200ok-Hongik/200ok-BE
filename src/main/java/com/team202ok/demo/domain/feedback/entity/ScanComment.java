package com.team202ok.demo.domain.feedback.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "scan_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScanComment extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_result_id", nullable = false)
    private Long scanResultId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Builder
    private ScanComment(Long scanResultId, Long userId, String comment) {
        this.scanResultId = scanResultId;
        this.userId = userId;
        this.comment = comment;
    }
}
