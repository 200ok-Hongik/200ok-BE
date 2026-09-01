package com.team202ok.demo.domain.disposal.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.ai.entity.ScanResult;
import com.team202ok.demo.domain.feedback.entity.UserFeedback;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "disposal_decisions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisposalDecision extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_result_id", nullable = false)
    private Long scanResultId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_result_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_disposal_decisions_scan"))
    private ScanResult scanResult;

    @Column(name = "user_feedback_id", nullable = false)
    private Long userFeedbackId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_feedback_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_disposal_decisions_feedback"))
    private UserFeedback userFeedback;

    @Column(name = "applied_category_id", nullable = false)
    private Long appliedCategoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applied_category_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_disposal_decisions_category"))
    private TrashCategory appliedCategory;

    @Column(name = "category_source", nullable = false, length = 20)
    private String categorySource;

    @Column(name = "is_pass", nullable = false)
    private Boolean isPass;

    @Column(name = "guide_snapshot", nullable = false, columnDefinition = "TEXT")
    private String guideSnapshot;

    @Builder
    private DisposalDecision(Long scanResultId, Long userFeedbackId, Long appliedCategoryId,
                             String categorySource, Boolean isPass, String guideSnapshot) {
        this.scanResultId = scanResultId;
        this.userFeedbackId = userFeedbackId;
        this.appliedCategoryId = appliedCategoryId;
        this.categorySource = categorySource;
        this.isPass = isPass != null ? isPass : false;
        this.guideSnapshot = guideSnapshot;
    }
}
