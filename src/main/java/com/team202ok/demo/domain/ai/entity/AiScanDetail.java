package com.team202ok.demo.domain.ai.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.disposal.entity.ItemChecklist;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "ai_scan_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiScanDetail extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ai_scan_result_id", nullable = false)
    private Long aiScanResultId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_scan_result_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_ai_scan_details_result"))
    private AiScanResult aiScanResult;

    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checklist_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_ai_scan_details_checklist"))
    private ItemChecklist checklist;

    @Column(name = "status_value", nullable = false, length = 100)
    private String statusValue;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Builder
    private AiScanDetail(Long aiScanResultId, Long checklistId,
                         String statusValue, BigDecimal confidence) {
        this.aiScanResultId = aiScanResultId;
        this.checklistId = checklistId;
        this.statusValue = statusValue;
        this.confidence = confidence;
    }
}
