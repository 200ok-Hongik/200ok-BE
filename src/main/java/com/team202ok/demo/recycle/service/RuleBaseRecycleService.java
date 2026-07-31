//// 구현체 - 현재는 하나라 자동 주입됨
//package com.team202ok.demo.recycle.service;
//
//import com.team202ok.demo.recycle.dto.RecycleRuleReq;
//import com.team202ok.demo.recycle.dto.RecycleRuleRes;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//public class RuleBaseRecycleService implements RecycleService {
//
//    private final Map<String, ItemRuleStrategy> strategyMap;
//
//    // 스프링이 ItemRuleStrategy 구현체 전부 모아서 Map으로 만들어줌
//    public RuleBaseRecycleService(List<ItemRuleStrategy> strategies) {
//        this.strategyMap = strategies.stream()
//                .collect(Collectors.toMap(
//                        ItemRuleStrategy::getItemCode, // 각 규칙의 getItemCode() 호출해서 key로...
//                        s -> s //규칙(s)를 받아서 s(그대로) 반환
//                ));
//    }
//
//    @Override
//    public RecycleRuleRes check(RecycleRuleReq request) {
//
//        ItemRuleStrategy strategy = strategyMap.get(request.getItemCode());
//
//        if (strategy == null)
//            throw new IllegalArgumentException("지원하지 않는 품목");
//
//        return strategy.check(request);
//    }
//}
//
//
