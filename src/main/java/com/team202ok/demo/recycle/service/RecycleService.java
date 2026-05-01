// 인터페이스
package com.team202ok.demo.recycle.service;

import com.team202ok.demo.recycle.dto.RecycleRuleReq;
import com.team202ok.demo.recycle.dto.RecycleRuleRes;

public interface RecycleService {
    RecycleRuleRes check(RecycleRuleReq request);
}