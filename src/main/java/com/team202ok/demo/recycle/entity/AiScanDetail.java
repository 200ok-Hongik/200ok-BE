package com.team202ok.demo.recycle.entity;

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

    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

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