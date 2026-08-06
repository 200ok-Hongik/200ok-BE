package com.team202ok.demo.domain.calendar.entity;

import com.team202ok.demo.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "user_calendars")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCalendar extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long disposalDecisionId;

    @Column(nullable = false)
    private Long trashCategoryId;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private Boolean isCompleted;

    @Builder
    private UserCalendar(Long userId, Long disposalDecisionId, Long trashCategoryId, LocalDateTime scheduledAt, Boolean isCompleted) {
        this.userId = userId;
        this.disposalDecisionId = disposalDecisionId;
        this.trashCategoryId = trashCategoryId;
        this.scheduledAt = scheduledAt;
        this.isCompleted = isCompleted != null ? isCompleted : false;
    }

    public void complete() {
        this.isCompleted = true;
    }
}
