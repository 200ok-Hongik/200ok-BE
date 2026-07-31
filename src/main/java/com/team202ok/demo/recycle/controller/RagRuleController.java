//package com.team202ok.demo.recycle.controller;
//
//import com.team202ok.demo.recycle.entity.Rule;
//import com.team202ok.demo.recycle.repository.RagRecycleRepository;
//import com.team202ok.demo.recycle.service.DisposalCrawlerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Sort;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/admin/crawl")
//public class RagRuleController {
//
//    private final DisposalCrawlerService disposalCrawlerService;
//    private final RagRecycleRepository ragRecycleRepository;
//
//    @PostMapping("/pet-bottle")
//    public String crawlPetBottle() {
//        Rule rule = disposalCrawlerService.crawlPetBottle();
//        return "페트병 규칙 저장 완료: " + rule.getItemName();
//    }
//
//    @PostMapping("/rule")
//    public String crawlRule(
//            @RequestParam String itemName,
//            @RequestParam(required = false) String niIdx
//    ) {
//        Rule rule = disposalCrawlerService.crawlDisposalRule(itemName, niIdx);
//        return "규칙 저장 완료: " + rule.getItemName();
//    }
//
//    @GetMapping("/rules")
//    public List<Rule> findRules() {
//        return ragRecycleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
//    }
//}
