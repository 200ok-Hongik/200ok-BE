package com.team202ok.demo.domain.region.service;

import com.team202ok.demo.domain.region.entity.RegionSchedule;
import com.team202ok.demo.domain.region.repository.RegionScheduleRepository;
import com.team202ok.demo.global.exception.code.GeneralErrorCode;
import com.team202ok.demo.global.exception.custom.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegionDischargeService {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final List<String> WEEKDAYS = List.of("월", "화", "수", "목", "금", "토", "일");
    private static final Set<String> OTHER_RECYCLABLES =
            Set.of("PLASTIC", "CAN", "GLASS_BOTTLE", "PAPER", "PAPER_PACK", "STYROFOAM");

    private final RegionScheduleRepository regionScheduleRepository;

    public Optional<Schedule> findSchedule(String regionCode, String categoryCode) {
        return regionScheduleRepository.findByRegionCode(regionCode)
                .map(schedule -> resolve(schedule, categoryCode));
    }

    public LocalDateTime now() {
        return LocalDateTime.now(SEOUL);
    }

    private Schedule resolve(RegionSchedule schedule, String categoryCode) {
        if (!schedule.getRegionCode().startsWith("MAPO_")) {
            return new Schedule(schedule.getDischargeDays(), schedule.getDischargeTime(), false);
        }

        // 2024 마포구의회 행정사무감사 자료(도화동), 문전수거 안내
        // https://council.mapo.seoul.kr/attach/record/MAPO/appendix/a09/A0009782.pdf
        String days = schedule.getDischargeDays();
        if ("CLEAR_PET_BOTTLE".equals(categoryCode) || "VINYL".equals(categoryCode)) {
            days = "수";
        } else if (categoryCode != null && OTHER_RECYCLABLES.contains(categoryCode)) {
            days = "일,월,화,목";
        }
        return new Schedule(days, schedule.getDischargeTime(), true);
    }

    public record Schedule(String dischargeDays, String dischargeTime, boolean mapoCurbside) {
        public String dischargeTimeOn(LocalDate date) {
            int month = date.getMonthValue();
            return mapoCurbside && month >= 4 && month <= 10 ? "19:00~24:00" : dischargeTime;
        }

        public LocalDateTime nextOccurrence(LocalDateTime now) {
            List<String> days = Arrays.stream(dischargeDays.split(",", -1)).map(String::trim).toList();
            if (!WEEKDAYS.containsAll(days)) {
                throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "지역 배출 요일 형식이 올바르지 않습니다.");
            }

            for (int offset = 0; offset <= 7; offset++) {
                LocalDate date = now.toLocalDate().plusDays(offset);
                if (!days.contains(WEEKDAYS.get(date.getDayOfWeek().getValue() - 1))) {
                    continue;
                }
                String[] times = dischargeTimeOn(date).split("~", -1);
                try {
                    LocalDateTime start = date.atTime(LocalTime.parse(times[0].trim()));
                    if (!now.isAfter(start)) {
                        return start;
                    }
                    if (times.length == 2) {
                        String endTime = times[1].trim();
                        LocalDateTime end = "24:00".equals(endTime)
                                ? date.plusDays(1).atStartOfDay()
                                : date.atTime(LocalTime.parse(endTime));
                        if (now.isBefore(end)) {
                            return now;
                        }
                    }
                } catch (DateTimeParseException e) {
                    throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "지역 배출 시간 형식이 올바르지 않습니다.");
                }
            }
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "지역 배출 요일 형식이 올바르지 않습니다.");
        }
    }
}
