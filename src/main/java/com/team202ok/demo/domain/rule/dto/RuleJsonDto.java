package com.team202ok.demo.domain.rule.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleJsonDto {
    private String guName;          // 구 이름 (예: 마포구)
    private String dongName;        // 동 이름 (예: 서교동)
    private String dischargeDays;   // 배출 요일
    private String dischargeTime;   // 배출 시간
}