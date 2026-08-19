package com.team202ok.demo.domain.notification.controller;

import com.team202ok.demo.domain.home.dto.HomeRes;
import com.team202ok.demo.domain.home.service.HomeService;
import com.team202ok.demo.domain.notification.dto.PushDeviceReq;
import com.team202ok.demo.domain.notification.dto.PushDeviceRes;
import com.team202ok.demo.domain.notification.dto.TestPushReq;
import com.team202ok.demo.domain.notification.service.NotificationService;
import com.team202ok.demo.domain.notification.service.PushDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "알림", description = "사용자에게 생성된 최근 알림을 조회합니다.")
public class NotificationController {
    private final HomeService homeService;
    private final PushDeviceService pushDeviceService;
    private final NotificationService notificationService;

    @GetMapping("/recent")
    @Operation(summary = "최근 알림 조회", description = "사용자의 최근 알림 10건을 최신순으로 조회합니다.")
    public List<HomeRes.NotificationItem> getRecentNotifications(@Parameter(description = "임시 사용자 ID", example = "1") @RequestParam Long userId) {
        return homeService.getRecentNotifications(userId);
    }

    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "푸시 기기 등록", description = "프론트에서 발급한 FCM 토큰을 사용자 기기로 등록하거나 갱신합니다.")
    public PushDeviceRes registerDevice(@RequestParam Long userId, @Valid @RequestBody PushDeviceReq.Register request) {
        return pushDeviceService.register(userId, request);
    }

    @DeleteMapping("/devices")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "푸시 기기 삭제", description = "로그아웃한 기기의 푸시 토큰을 삭제합니다.")
    public void removeDevice(@RequestParam Long userId, @Valid @RequestBody PushDeviceReq.Remove request) {
        pushDeviceService.remove(userId, request);
    }

    @PostMapping("/test")
    @Operation(summary = "테스트 푸시 발송", description = "등록된 기기로 개발용 분리배출 알림을 발송합니다. 현재 fake 모드에서는 서버 로그로 확인합니다.")
    public int sendTestPush(@RequestParam Long userId, @RequestBody(required = false) TestPushReq request) {
        return notificationService.sendTestPush(userId, request == null ? null : request.itemName());
    }
}
