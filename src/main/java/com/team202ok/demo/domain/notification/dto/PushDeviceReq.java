package com.team202ok.demo.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class PushDeviceReq {
    private PushDeviceReq() {}

    public record Register(
            @NotBlank @Size(max = 500) String token,
            @NotBlank @Pattern(regexp = "WEB|ANDROID|IOS") String deviceType) {}

    public record Remove(@NotBlank @Size(max = 500) String token) {}
}
