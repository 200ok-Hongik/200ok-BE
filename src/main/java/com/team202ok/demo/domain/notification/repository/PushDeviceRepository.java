package com.team202ok.demo.domain.notification.repository;

import com.team202ok.demo.domain.notification.entity.PushDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {
    Optional<PushDevice> findByToken(String token);
    List<PushDevice> findByUserId(Long userId);
    long deleteByUserIdAndToken(Long userId, String token);
}
