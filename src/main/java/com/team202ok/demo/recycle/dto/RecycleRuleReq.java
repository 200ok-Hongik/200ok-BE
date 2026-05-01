package com.team202ok.demo.recycle.dto;

import lombok.Getter;
import java.util.Map;

@Getter
public class RecycleRuleReq {
    private String item;
    private String region;
    private Map<String, Object> conditions;
}