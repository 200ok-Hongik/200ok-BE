package com.team202ok.demo.global.config;

import com.team202ok.demo.domain.analysis.entity.*;
import com.team202ok.demo.domain.analysis.repository.*;
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
    private final ItemChecklistRepository itemChecklistRepository;
    private final DisposalGuideRepository disposalGuideRepository;
    private final UserRepository userRepository;
    private final ScanResultRepository scanResultRepository;

    @Override
    public void run(String... args) throws Exception {

        TrashCategory petBottle = trashCategoryRepository.findByCode("PET_BOTTLE")
                .orElse(null);

        if (petBottle == null) {
            System.out.println("⚠️ PET_BOTTLE 카테고리가 아직 없어 목데이터 초기화를 건너뜁니다.");
            return;
        }

        // item_checklists
        if (itemChecklistRepository.count() == 0) {
            itemChecklistRepository.save(ItemChecklist.builder()
                    .trashCategoryId(petBottle.getId())
                    .checkItemName("isTransparent")
                    .guideMessage("투명 페트병인지 확인하세요")
                    .displayOrder(1)
                    .triggerValue("false")
                    .build());

            itemChecklistRepository.save(ItemChecklist.builder()
                    .trashCategoryId(petBottle.getId())
                    .checkItemName("hasLabel")
                    .guideMessage("라벨을 제거하세요")
                    .displayOrder(2)
                    .triggerValue("true")
                    .build());

            itemChecklistRepository.save(ItemChecklist.builder()
                    .trashCategoryId(petBottle.getId())
                    .checkItemName("isEmpty")
                    .guideMessage("내용물을 비우고 헹구세요")
                    .displayOrder(3)
                    .triggerValue("false")
                    .build());

            itemChecklistRepository.save(ItemChecklist.builder()
                    .trashCategoryId(petBottle.getId())
                    .checkItemName("hasCap")
                    .guideMessage("뚜껑을 분리하세요")
                    .displayOrder(4)
                    .triggerValue("true")
                    .build());

            itemChecklistRepository.save(ItemChecklist.builder()
                    .trashCategoryId(petBottle.getId())
                    .checkItemName("isContaminated")
                    .guideMessage("이물질을 제거하고 깨끗이 헹구세요")
                    .displayOrder(5)
                    .triggerValue("true")
                    .build());

            itemChecklistRepository.save(ItemChecklist.builder()
                    .trashCategoryId(petBottle.getId())
                    .checkItemName("isCrushed")
                    .guideMessage("페트병을 압착하세요")
                    .displayOrder(6)
                    .triggerValue("false")
                    .build());

            System.out.println("✅ item_checklists 목데이터 등록 완료!");
        }

        // disposal_guides
        if (disposalGuideRepository.count() == 0) {
            disposalGuideRepository.save(DisposalGuide.builder()
                    .trashCategoryId(petBottle.getId())
                    .guideMessage("페트병은 라벨과 뚜껑을 분리하고 압착하여 배출해주세요.")
                    .cautionMessage("이물질이 남아있으면 재활용이 어렵습니다.")
                    .isActive(true)
                    .build());

            System.out.println("✅ disposal_guides 목데이터 등록 완료!");
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
