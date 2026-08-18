package com.team202ok.demo.domain.user.service;

import com.team202ok.demo.domain.user.dto.UserRes;
import com.team202ok.demo.domain.user.dto.UserReq;

public interface UserService {
    UserRes.Profile updateRegion(Long userId, Long regionId);

    UserRes.Profile getProfile(Long userId);

    UserRes.Profile updateProfile(Long userId, UserReq.UpdateProfile request);
}
