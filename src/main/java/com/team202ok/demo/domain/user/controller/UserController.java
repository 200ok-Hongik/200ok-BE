package com.team202ok.demo.domain.user.controller;

import com.team202ok.demo.domain.user.dto.UserReq;
import com.team202ok.demo.domain.user.dto.UserRes;
import com.team202ok.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PatchMapping("/me/region")
    public UserRes updateRegion(
            @RequestParam Long userId,
            @RequestBody UserReq.UpdateRegion request
    ) {
        return userService.updateRegion(userId, request.regionId());
    }
}
