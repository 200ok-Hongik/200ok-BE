package com.team202ok.demo.domain.notification.service;

import com.team202ok.demo.domain.notification.dto.PushDeviceReq;
import com.team202ok.demo.domain.notification.dto.PushDeviceRes;
import com.team202ok.demo.domain.notification.entity.PushDevice;
import com.team202ok.demo.domain.notification.repository.PushDeviceRepository;
import com.team202ok.demo.domain.user.repository.UserRepository;
import com.team202ok.demo.global.exception.code.GeneralErrorCode;
import com.team202ok.demo.global.exception.custom.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PushDeviceServiceImpl implements PushDeviceService {
    private final PushDeviceRepository pushDeviceRepository;
    private final UserRepository userRepository;

    @Override
    public PushDeviceRes register(Long userId, PushDeviceReq.Register request) {
        if (!userRepository.existsById(userId)) {
            throw new ProjectException(GeneralErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        PushDevice device = pushDeviceRepository.findByToken(request.token())
                .map(existing -> {
                    existing.registerTo(userId, request.deviceType());
                    return existing;
                })
                .orElseGet(() -> pushDeviceRepository.save(PushDevice.builder()
                        .userId(userId).token(request.token()).deviceType(request.deviceType()).build()));
        return new PushDeviceRes(device.getId(), device.getDeviceType());
    }

    @Override
    public void remove(Long userId, PushDeviceReq.Remove request) {
        pushDeviceRepository.deleteByUserIdAndToken(userId, request.token());
    }
}
