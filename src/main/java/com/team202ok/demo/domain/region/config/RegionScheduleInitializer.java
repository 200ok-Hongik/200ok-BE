package com.team202ok.demo.domain.region.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team202ok.demo.domain.region.dto.RegionScheduleJson;
import com.team202ok.demo.domain.region.entity.Region;
import com.team202ok.demo.domain.region.repository.RegionRepository;
import com.team202ok.demo.domain.region.entity.RegionSchedule;
import com.team202ok.demo.domain.region.repository.RegionScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RegionScheduleInitializer implements ApplicationRunner {

    private final RegionRepository regionRepository;
    private final RegionScheduleRepository regionScheduleRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource("regional_schedules.json");
        List<RegionScheduleJson> jsonList;
        try (var input = resource.getInputStream()) {
            jsonList = objectMapper.readValue(input, new TypeReference<List<RegionScheduleJson>>() {});
        }

        for (RegionScheduleJson json : jsonList) {
            regionRepository.findByRegionCode(json.getRegionCode())
                    .orElseGet(() -> regionRepository.save(Region.builder()
                            .regionCode(json.getRegionCode())
                            .sido("서울특별시")
                            .gugun(json.getGuName())
                            .dong(json.getDongName())
                            .build()));
            RegionSchedule schedule = regionScheduleRepository.findByRegionCode(json.getRegionCode())
                    .map(existing -> {
                        existing.update(json.getDischargeDays(), json.getDischargeTime());
                        return existing;
                    })
                    .orElseGet(() -> RegionSchedule.builder()
                            .regionCode(json.getRegionCode())
                            .dischargeDays(json.getDischargeDays())
                            .dischargeTime(json.getDischargeTime())
                            .build());
            regionScheduleRepository.save(schedule);
        }

        log.info("마포구 지역 배출 일정 초기화 완료 (총 {}건)", jsonList.size());
    }
}
