package com.team202ok.demo.domain.feedback.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.ai.entity.ScanResult;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_feedbacks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFeedback extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_result_id", nullable = false)
    private Long scanResultId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_result_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_feedbacks_scan"))
    private ScanResult scanResult;

    @Column(name = "corrected_category_id")
    private Long correctedCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corrected_category_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_feedbacks_category"))
    private TrashCategory correctedCategory;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Builder
    private UserFeedback(Long scanResultId, Long correctedCategoryId, String comment) {
        this.scanResultId = scanResultId;
        this.correctedCategoryId = correctedCategoryId;
        this.comment = comment;
    }
}
