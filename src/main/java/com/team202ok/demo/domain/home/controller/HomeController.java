package com.team202ok.demo.domain.home.controller;

import com.team202ok.demo.domain.home.dto.HomeRes;
import com.team202ok.demo.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "홈", description = "오늘의 배출 일정과 최근 알림을 홈 화면용으로 조회합니다.")
public class HomeController {
    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "홈 요약 조회", description = "오늘의 배출 일정과 최근 알림 목록을 함께 조회합니다.")
    public HomeRes.Summary getSummary(@Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return homeService.getSummary(userId);
    }

    @GetMapping("/today")
    @Operation(summary = "오늘의 배출 일정 조회", description = "오늘 예정된 배출 일정 목록을 조회합니다.")
    public List<HomeRes.TodaySchedule> getTodaySchedules(@Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        return homeService.getTodaySchedules(userId);
    }
}
