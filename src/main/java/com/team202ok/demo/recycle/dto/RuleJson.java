package com.team202ok.demo.recycle.dto;

import lombok.Getter;
import java.util.Map;

@Getter
public class RuleJson {
    private String ruleId;
    private String item;
    private RegionJson region;
    private Map<String, Object> conditions;
    private String verdict;
    private String requiredAction;
    private String disposalMethod;
    private String basis;
    private int priority;

    @Getter
    public static class RegionJson {
        private String sido;
        private String sigungu;
    }
}