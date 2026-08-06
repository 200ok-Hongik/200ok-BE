package com.team202ok.demo.domain.notification.controller;

import com.team202ok.demo.domain.home.dto.HomeRes;
import com.team202ok.demo.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final HomeService homeService;

    @GetMapping("/recent")
    public List<HomeRes.NotificationItem> getRecentNotifications(@RequestParam Long userId) {
        return homeService.getRecentNotifications(userId);
    }
}
