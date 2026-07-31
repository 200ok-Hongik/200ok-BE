//package com.team202ok.demo.recycle.config;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.team202ok.demo.recycle.domain.RecycleRule;
//import com.team202ok.demo.recycle.domain.Region;
//import com.team202ok.demo.recycle.repository.RegionRepository;
//import com.team202ok.demo.recycle.dto.RuleJson;
//import com.team202ok.demo.recycle.repository.RuleRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class RuleJsonInitializer implements ApplicationRunner {
//
//    private final RuleRepository ruleRepository;
//    private final RegionRepository regionRepository;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        if (ruleRepository.count() > 0) return;
//
//        ClassPathResource resource = new ClassPathResource("rules.json");
//        List<RuleJson> ruleJsonList = objectMapper.readValue(
//                resource.getInputStream(),
//                new TypeReference<List<RuleJson>>() {}
//        );
//
//        for (RuleJson ruleJson : ruleJsonList) {
//            String regionName = ruleJson.getRegion().getSido() + " " + ruleJson.getRegion().getSigungu();
//
//            Region region = regionRepository
//                    .findByRegion(regionName)
//                    .orElseGet(() -> regionRepository.save(
//                            Region.builder()
//                                    .region(regionName)
//                                    .build()
//                    ));
//
//            ruleRepository.save(
//                    RecycleRule.builder()
//                            .ruleId(ruleJson.getRuleId())
//                            .item(ruleJson.getItem())
//                            .region(region)
//                            .conditions(ruleJson.getConditions())
//                            .verdict(ruleJson.getVerdict())
//                            .requiredAction(ruleJson.getRequiredAction())
//                            .disposalMethod(ruleJson.getDisposalMethod())
//                            .basis(ruleJson.getBasis())
//                            .priority(ruleJson.getPriority())
//                            .build()
//            );
//        }
//    }
//}