package com.team202ok.demo.domain.feedback.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.ai.entity.ScanResult;
import com.team202ok.demo.domain.user.entity.User;
import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_result_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_scan_comments_scan"))
    private ScanResult scanResult;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_scan_comments_user"))
    private User user;

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
