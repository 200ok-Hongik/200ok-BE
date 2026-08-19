package com.team202ok.demo.domain.notification.service;

import com.team202ok.demo.domain.notification.dto.PushDeviceReq;
import com.team202ok.demo.domain.notification.dto.PushDeviceRes;

public interface PushDeviceService {
    PushDeviceRes register(Long userId, PushDeviceReq.Register request);
    void remove(Long userId, PushDeviceReq.Remove request);
}
