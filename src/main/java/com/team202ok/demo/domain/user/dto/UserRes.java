package com.team202ok.demo.domain.user.dto;

public record UserRes(Long userId, String name, String profileImageUrl, String regionCode) {

    public record Profile(Long userId, String name, String profileImageUrl, Region region,
                          boolean notificationEnabled) {}
    public record Region(Long regionId, String sido, String gugun, String dong) {}
}
