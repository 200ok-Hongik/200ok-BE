package com.team202ok.demo.domain.region.service;

import com.team202ok.demo.domain.region.entity.RegionSchedule;
import com.team202ok.demo.domain.region.repository.RegionScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegionDischargeServiceTest {
    private final RegionScheduleRepository schedules = mock(RegionScheduleRepository.class);
    private final RegionDischargeService service = new RegionDischargeService(schedules);

    @BeforeEach
    void setUp() {
        when(schedules.findByRegionCode("MAPO_SEOGYO")).thenReturn(Optional.of(RegionSchedule.builder()
                .regionCode("MAPO_SEOGYO").dischargeDays("일,월,화,수,목").dischargeTime("18:00~24:00").build()));
    }

    @Test
    void resolvesMapoWednesdayExceptionsByCategory() {
        assertThat(mapo("CLEAR_PET_BOTTLE").dischargeDays()).isEqualTo("수");
        assertThat(mapo("VINYL").dischargeDays()).isEqualTo("수");
        assertThat(mapo("CAN").dischargeDays()).isEqualTo("일,월,화,목");
        assertThat(mapo("UNKNOWN").dischargeDays()).isEqualTo("일,월,화,수,목");
    }

    @Test
    void schedulesTodayAtOpeningOrDuringTheDischargeWindow() {
        var schedule = mapo("CLEAR_PET_BOTTLE");
        assertThat(schedule.nextOccurrence(LocalDateTime.parse("2026-11-25T17:00")))
                .isEqualTo(LocalDateTime.parse("2026-11-25T18:00"));
        assertThat(schedule.nextOccurrence(LocalDateTime.parse("2026-11-25T18:00")))
                .isEqualTo(LocalDateTime.parse("2026-11-25T18:00"));
        assertThat(schedule.nextOccurrence(LocalDateTime.parse("2026-11-25T20:00")))
                .isEqualTo(LocalDateTime.parse("2026-11-25T20:00"));
        assertThat(schedule.nextOccurrence(LocalDateTime.parse("2026-11-26T00:00")))
                .isEqualTo(LocalDateTime.parse("2026-12-02T18:00"));
    }

    @Test
    void usesTheSeasonOfTheNextEligibleDateAndSkipsFridayAndSaturday() {
        var cans = mapo("CAN");
        var petBottles = mapo("CLEAR_PET_BOTTLE");
        assertThat(petBottles.nextOccurrence(LocalDateTime.parse("2026-03-31T20:00")))
                .isEqualTo(LocalDateTime.parse("2026-04-01T19:00"));
        assertThat(cans.nextOccurrence(LocalDateTime.parse("2026-04-01T20:00")))
                .isEqualTo(LocalDateTime.parse("2026-04-02T19:00"));
        assertThat(cans.nextOccurrence(LocalDateTime.parse("2026-10-30T20:00")))
                .isEqualTo(LocalDateTime.parse("2026-11-01T18:00"));
    }

    @Test
    void keepsTheConfiguredWindowForOtherRegionsAndHonorsClosingTime() {
        when(schedules.findByRegionCode("OTHER")).thenReturn(Optional.of(RegionSchedule.builder()
                .regionCode("OTHER").dischargeDays("수").dischargeTime("17:00~20:00").build()));
        var schedule = service.findSchedule("OTHER", "CAN").orElseThrow();
        assertThat(schedule.nextOccurrence(LocalDateTime.parse("2026-04-01T20:00")))
                .isEqualTo(LocalDateTime.parse("2026-04-08T17:00"));
    }

    private RegionDischargeService.Schedule mapo(String categoryCode) {
        return service.findSchedule("MAPO_SEOGYO", categoryCode).orElseThrow();
    }
}
