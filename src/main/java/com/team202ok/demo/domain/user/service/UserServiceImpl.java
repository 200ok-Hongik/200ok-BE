package com.team202ok.demo.domain.user.service;

import com.team202ok.demo.domain.region.entity.Region;
import com.team202ok.demo.domain.region.repository.RegionRepository;
import com.team202ok.demo.domain.user.dto.UserRes;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.domain.user.repository.UserRepository;
import com.team202ok.demo.global.exception.GeneralErrorCode;
import com.team202ok.demo.global.exception.ProjectException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RegionRepository regionRepository;

    @Override
    public UserRes updateRegion(Long userId, Long regionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "지역을 찾을 수 없습니다."));

        user.updateRegion(region.getRegionCode());
        return new UserRes(user.getId(), user.getName(), user.getProfileImageUrl(), user.getRegionCode());
    }

    @Override
    public UserRes.Profile getProfile(Long userId) {
        User user = findUser(userId);
        UserRes.Region region = user.getRegionCode() == null ? null : regionRepository.findByRegionCode(user.getRegionCode())
                .map(r -> new UserRes.Region(r.getId(), r.getSido(), r.getGugun(), r.getDong())).orElse(null);
        return new UserRes.Profile(user.getId(), user.getName(), user.getProfileImageUrl(), region);
    }

    @Override
    public UserRes.Profile updateProfile(Long userId, com.team202ok.demo.domain.user.dto.UserReq.UpdateProfile request) {
        User user = findUser(userId);
        user.updateProfile(request.name(), request.profileImageUrl());
        return getProfile(userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
