package com.team202ok.demo.recycle.entity;

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

    @Column(name = "user_category_id")
    private Long userCategoryId;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Builder
    private UserFeedback(Long scanResultId, Long userCategoryId, String comment) {
        this.scanResultId = scanResultId;
        this.userCategoryId = userCategoryId;
        this.comment = comment;
    }
}