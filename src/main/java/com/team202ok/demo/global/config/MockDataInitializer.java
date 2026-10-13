package com.team202ok.demo.global.config;

import com.team202ok.demo.domain.ai.entity.*;
import com.team202ok.demo.domain.ai.repository.*;
import com.team202ok.demo.domain.disposal.entity.*;
import com.team202ok.demo.domain.disposal.repository.*;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // DataInitializer(0), RegionScheduleInitializer(1) 이후 실행
@RequiredArgsConstructor
public class MockDataInitializer implements CommandLineRunner {

    private final TrashCategoryRepository trashCategoryRepository;
    private final UserRepository userRepository;
    private final ScanResultRepository scanResultRepository;

    @Override
    public void run(String... args) throws Exception {

        TrashCategory petBottle = trashCategoryRepository.findByCode("CLEAR_PET_BOTTLE")
                .orElse(null);

        if (petBottle == null) {
            System.out.println("⚠️ CLEAR_PET_BOTTLE 카테고리가 아직 없어 목데이터 초기화를 건너뜁니다.");
            return;
        }

        // users (RegionScheduleInitializer가 넣은 MAPO_SEOGYO 코드 사용)
        User testUser;
        if (userRepository.count() == 0) {
            testUser = userRepository.save(User.builder()
                    .kakaoId("kakao_test_001")
                    .name("김지현")
                    .regionCode("MAPO_SEOGYO")
                    .isNotificationEnabled(true)
                    .build());

            System.out.println("✅ users 목데이터 등록 완료! (userId=" + testUser.getId() + ")");
        } else {
            testUser = userRepository.findAll().get(0);
        }

        // scan_results
        if (scanResultRepository.count() == 0) {
            ScanResult testScan = scanResultRepository.save(ScanResult.builder()
                    .userId(testUser.getId())
                    .imageUrl("https://example.com/test-image.jpg")
                    .build());

            System.out.println("✅ scan_results 목데이터 등록 완료! (scanResultId=" + testScan.getId() + ")");
        }
    }
}
