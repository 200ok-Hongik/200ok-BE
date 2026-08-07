package com.team202ok.demo.domain.calendar.controller;

import com.team202ok.demo.domain.calendar.dto.CalendarReq;
import com.team202ok.demo.domain.calendar.dto.CalendarRes;
import com.team202ok.demo.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
@Tag(name = "배출 캘린더", description = "분리배출 일정 저장, 조회 및 완료 처리를 제공합니다.")
public class CalendarController {
    private final CalendarService calendarService;

    @PostMapping
    @Operation(summary = "배출 일정 저장", description = "최종 분리배출 판단을 캘린더에 저장합니다. scheduledAt이 없으면 지역 배출 요일과 시간으로 자동 계산합니다.")
    public CalendarRes create(@Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId, @RequestBody CalendarReq request) { return calendarService.create(userId, request); }

    @GetMapping
    @Operation(summary = "기간별 배출 일정 조회", description = "시작일과 종료일 사이의 사용자의 배출 일정을 조회합니다.")
    public List<CalendarRes> getCalendars(@Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return calendarService.getCalendars(userId, startDate, endDate);
    }

    @GetMapping("/{calendarId}")
    @Operation(summary = "배출 일정 상세 조회", description = "특정 캘린더 일정의 품목과 예정 시간을 조회합니다.")
    public CalendarRes getCalendar(@Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId, @Parameter(description = "캘린더 ID", example = "1") @PathVariable Long calendarId) { return calendarService.getCalendar(userId, calendarId); }

    @PatchMapping("/{calendarId}/complete")
    @Operation(summary = "배출 완료 처리", description = "선택한 배출 일정을 완료 상태로 변경합니다.")
    public CalendarRes complete(@Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId, @Parameter(description = "캘린더 ID", example = "1") @PathVariable Long calendarId) { return calendarService.complete(userId, calendarId); }
}
