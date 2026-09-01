package com.team202ok.demo.domain.disposal.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "disposal_decision_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisposalDecisionDetail extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disposal_decision_id", nullable = false)
    private Long disposalDecisionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "disposal_decision_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_decision_details_decision"))
    private DisposalDecision disposalDecision;

    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_decision_details_checklist"))
    private ItemChecklist checklist;

    @Column(name = "status_value", nullable = false, length = 100)
    private String statusValue;

    @Builder
    private DisposalDecisionDetail(Long disposalDecisionId, Long checklistId, String statusValue) {
        this.disposalDecisionId = disposalDecisionId;
        this.checklistId = checklistId;
        this.statusValue = statusValue;
    }
}
