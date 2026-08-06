package com.team202ok.demo.domain.feedback.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;

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

    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

    @Column(name = "status_value", nullable = false, length = 100)
    private String statusValue;

    @Builder
    private UserFeedbackDetail(Long userFeedbackId, Long checklistId, String statusValue) {
        this.userFeedbackId = userFeedbackId;
        this.checklistId = checklistId;
        this.statusValue = statusValue;
    }
}