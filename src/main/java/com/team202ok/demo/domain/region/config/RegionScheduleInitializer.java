package com.team202ok.demo.domain.region.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team202ok.demo.domain.region.dto.RegionScheduleJson;
import com.team202ok.demo.domain.region.entity.Region;
import com.team202ok.demo.domain.region.repository.RegionRepository;
import com.team202ok.demo.domain.region.entity.RegionSchedule;
import com.team202ok.demo.domain.region.repository.RegionScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RegionScheduleInitializer implements ApplicationRunner {

    private final RegionRepository regionRepository;
    private final RegionScheduleRepository regionScheduleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. JSON 파일 읽기 (이름은 상황에 맞게 변경 가능)
        ClassPathResource resource = new ClassPathResource("regional_schedules.json");
        List<RegionScheduleJson> jsonList = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<RegionScheduleJson>>() {}
        );

        // 2. 현재 JSON에 있는 regionCode 목록 추출 (삭제 동기화를 위함)
        Set<String> currentRegionCodes = jsonList.stream()
                .map(RegionScheduleJson::getRegionCode)
                .collect(Collectors.toSet());

        // 3. Upsert 로직 (있으면 업데이트, 없으면 신규 생성)
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

        // 4. 삭제 로직 (DB에는 있지만 JSON 파일에는 없는 지역 데이터 삭제)
        /*
        scheduleRepository.findAll().stream()
                .filter(schedule -> !currentRegionCodes.contains(schedule.getRegionCode()))
                .forEach(scheduleRepository::delete);
        */

        log.info("MVP 테스트용 마포구 지역 배출 일정 초기화 완료! (총 {}건)", jsonList.size());
    }
}
