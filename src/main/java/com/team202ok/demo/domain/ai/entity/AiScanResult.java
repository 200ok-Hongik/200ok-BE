package com.team202ok.demo.domain.ai.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "ai_scan_results")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiScanResult extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_result_id", nullable = false)
    private Long scanResultId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scan_result_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_ai_scan_results_scan"))
    private ScanResult scanResult;

    @Column(name = "ai_category_id", nullable = false)
    private Long aiCategoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ai_category_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_ai_scan_results_category"))
    private TrashCategory aiCategory;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Lob
    @Column(name = "raw_response", columnDefinition = "LONGTEXT")
    private String rawResponse;

    @Column(name = "object_id", length = 255)
    private String objectId;

    @Builder
    private AiScanResult(Long scanResultId, Long aiCategoryId, BigDecimal confidence,
                         String modelVersion, String rawResponse, String objectId) {
        this.scanResultId = scanResultId;
        this.aiCategoryId = aiCategoryId;
        this.confidence = confidence;
        this.modelVersion = modelVersion;
        this.rawResponse = rawResponse;
        this.objectId = objectId;
    }
}
