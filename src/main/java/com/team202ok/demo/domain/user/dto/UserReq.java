package com.team202ok.demo.domain.user.dto;

import jakarta.validation.constraints.NotNull;

public final class UserReq {
    private UserReq() {
    }

    public record UpdateRegion(Long regionId) {
    }

    public record UpdateProfile(String name, String profileImageUrl) {
    }

    public record UpdateNotification(@NotNull Boolean enabled) {
    }
}
