package com.team202ok.demo.domain.notification.repository;

import com.team202ok.demo.domain.notification.entity.PushDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PushDeviceRepository extends JpaRepository<PushDevice, Long> {
    Optional<PushDevice> findByToken(String token);
    List<PushDevice> findByUserId(Long userId);
    @Modifying
    @Transactional
    @Query("delete from PushDevice device where device.userId = :userId and device.token = :token")
    int deleteByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);
}
