package com.team202ok.demo.domain.user.controller;

import com.team202ok.demo.domain.user.dto.UserReq;
import com.team202ok.demo.domain.user.dto.UserRes;
import com.team202ok.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "기본 배출 지역과 프로필 정보를 관리합니다.")
public class UserController {
    private final UserService userService;

    @PatchMapping("/me/region")
    @Operation(summary = "기본 지역 설정", description = "사용자의 기본 분리배출 지역을 설정하거나 변경합니다.")
    public UserRes.Profile updateRegion(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody UserReq.UpdateRegion request
    ) {
        return userService.updateRegion(userId, request.regionId());
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "사용자 이름, 프로필 이미지, 기본 지역과 알림 수신 상태를 조회합니다.")
    public UserRes.Profile getProfile(@Parameter(hidden = true) @AuthenticationPrincipal Long userId) { return userService.getProfile(userId); }

    @PatchMapping("/me")
    @Operation(summary = "프로필 수정", description = "사용자 이름과 프로필 이미지 URL을 수정합니다.")
    public UserRes.Profile updateProfile(@Parameter(hidden = true) @AuthenticationPrincipal Long userId, @RequestBody UserReq.UpdateProfile request) {
        return userService.updateProfile(userId, request);
    }

    @PatchMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 사진 변경", description = "JPEG, PNG 또는 WebP 파일을 업로드하고 변경된 프로필을 반환합니다.")
    public UserRes.Profile updateProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return userService.updateProfileImage(userId, image);
    }

    @PatchMapping("/me/notifications")
    @Operation(summary = "알림 수신 설정", description = "로그인 사용자의 분리배출 알림 수신 여부를 켜거나 끕니다.")
    public UserRes.Profile updateNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserReq.UpdateNotification request
    ) {
        return userService.updateNotification(userId, request.enabled());
    }
}
