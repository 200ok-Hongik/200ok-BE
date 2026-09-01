package com.team202ok.demo.domain.feedback.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.disposal.entity.ItemChecklist;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_feedback_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFeedbackDetail extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_feedback_id", nullable = false)
    private Long userFeedbackId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_feedback_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_feedback_details_feedback"))
    private UserFeedback userFeedback;

    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_feedback_details_checklist"))
    private ItemChecklist checklist;

    @Column(name = "corrected_status_value", nullable = false, length = 100)
    private String correctedStatusValue;

    @Builder
    private UserFeedbackDetail(Long userFeedbackId, Long checklistId, String correctedStatusValue) {
        this.userFeedbackId = userFeedbackId;
        this.checklistId = checklistId;
        this.correctedStatusValue = correctedStatusValue;
    }
}
