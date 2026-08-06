package com.team202ok.demo.domain.region.dto;

import lombok.Getter;

@Getter
public class RegionScheduleJson {
    private String regionCode;      // 예: "MAPO_SEOGYO"
    private String guName;          // 예: "마포구"
    private String dongName;        // 예: "서교동"
    private String dischargeDays;   // 예: "월,수,금"
    private String dischargeTime;   // 예: "18:00~24:00"
}