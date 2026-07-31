package com.team202ok.demo.recycle.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team202ok.demo.recycle.dto.RegionScheduleJson;
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

    // private final RegionScheduleRepository scheduleRepository;
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
            /*
            RegionSchedule schedule = scheduleRepository.findByRegionCode(json.getRegionCode())
                    .map(existing -> {
                        // 기존 데이터가 있으면 내용 업데이트
                        existing.update(
                                json.getGuName(),
                                json.getDongName(),
                                json.getDischargeDays(),
                                json.getDischargeTime()
                        );
                        return existing;
                    })
                    .orElseGet(() -> RegionSchedule.builder()
                            // 기존 데이터가 없으면 새로 빌드
                            .regionCode(json.getRegionCode())
                            .guName(json.getGuName())
                            .dongName(json.getDongName())
                            .dischargeDays(json.getDischargeDays())
                            .dischargeTime(json.getDischargeTime())
                            .build());

            scheduleRepository.save(schedule);
            */
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