package com.team202ok.demo.domain.calendar.entity;

import com.team202ok.demo.global.entity.BaseTimeEntity;
import com.team202ok.demo.domain.disposal.entity.DisposalDecision;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.user.entity.User;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_calendars_user"))
    private User user;

    @Column(name = "disposal_decision_id", nullable = false)
    private Long disposalDecisionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disposal_decision_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_calendars_decision"))
    private DisposalDecision disposalDecision;

    @Column(name = "trash_category_id", nullable = false)
    private Long trashCategoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trash_category_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_user_calendars_category"))
    private TrashCategory trashCategory;

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
