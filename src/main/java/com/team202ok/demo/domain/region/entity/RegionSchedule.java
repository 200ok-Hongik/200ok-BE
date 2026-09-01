package com.team202ok.demo.domain.region.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "region_schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionSchedule extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region_code", nullable = false, length = 50)
    private String regionCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_code", referencedColumnName = "region_code",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_region_schedules_region_code"))
    private Region region;

    @Column(name = "discharge_days", nullable = false, length = 50)
    private String dischargeDays; // 예: "월,수,금"

    @Column(name = "discharge_time", nullable = false, length = 50)
    private String dischargeTime; // 예: "18:00~24:00"

    @Builder
    private RegionSchedule(String regionCode, String dischargeDays, String dischargeTime) {
        this.regionCode = regionCode;
        this.dischargeDays = dischargeDays;
        this.dischargeTime = dischargeTime;
    }

    public void update(String dischargeDays, String dischargeTime) {
        this.dischargeDays = dischargeDays;
        this.dischargeTime = dischargeTime;
    }
}
