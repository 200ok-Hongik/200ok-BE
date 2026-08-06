package com.team202ok.demo.domain.user.service;

import com.team202ok.demo.domain.user.dto.UserRes;

public interface UserService {
    UserRes updateRegion(Long userId, Long regionId);
}
