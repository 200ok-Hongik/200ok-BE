package com.team202ok.demo.domain.notification.controller;

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

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림", description = "사용자에게 생성된 최근 알림을 조회합니다.")
public class NotificationController {
    private final HomeService homeService;

    @GetMapping("/recent")
    @Operation(summary = "최근 알림 조회", description = "사용자의 최근 알림 10건을 최신순으로 조회합니다.")
    public List<HomeRes.NotificationItem> getRecentNotifications(@Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId) {
        return homeService.getRecentNotifications(userId);
    }
}
