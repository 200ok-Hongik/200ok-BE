// 구현체 - 현재는 하나라 자동 주입됨
package com.team202ok.demo.recycle.service;

import com.team202ok.demo.recycle.domain.RecycleRule;
import com.team202ok.demo.recycle.dto.RecycleRuleReq;
import com.team202ok.demo.recycle.dto.RecycleRuleRes;
import com.team202ok.demo.recycle.repository.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleBaseRecycleService implements RecycleService {

    private final RuleRepository ruleRepository;

    @Override
    public RecycleRuleRes check(RecycleRuleReq request) {
        // 1. 품목 + 지역으로 규칙 목록 조회
        List<RecycleRule> rules = ruleRepository.findByItemAndRegion_Region(
                request.getItem(), request.getRegion()
        );

        // 2. 조건 매칭 + 우선순위 정렬 후 반환
        // 규칙 디비 생기면 구현
        return null;
    }
}