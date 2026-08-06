package com.team202ok.demo.domain.home.controller;

import com.team202ok.demo.domain.home.dto.HomeRes;
import com.team202ok.demo.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @GetMapping
    public HomeRes.Summary getSummary(@RequestParam Long userId) {
        return homeService.getSummary(userId);
    }

    @GetMapping("/today")
    public List<HomeRes.TodaySchedule> getTodaySchedules(@RequestParam Long userId) {
        return homeService.getTodaySchedules(userId);
    }
}
