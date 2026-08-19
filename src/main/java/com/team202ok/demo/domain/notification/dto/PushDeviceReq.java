package com.team202ok.demo.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class PushDeviceReq {
    private PushDeviceReq() {}

    public record Register(
            @NotBlank String token,
            @NotBlank @Pattern(regexp = "WEB|ANDROID|IOS") String deviceType) {}

    public record Remove(@NotBlank String token) {}
}
