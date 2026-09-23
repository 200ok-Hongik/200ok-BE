package com.team202ok.demo.domain.disposal.config;

import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// 추후 품목 완전 확정시 data.sql로 변환!!!!
@Component
@Order(0)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TrashCategoryRepository trashCategoryRepository;

    @Override
    public void run(String... args) {

        saveIfNotExists("CLEAR_PET_BOTTLE", "무색 페트병");
        saveIfNotExists("PLASTIC", "플라스틱");
        saveIfNotExists("CAN", "캔류");
        saveIfNotExists("GLASS_BOTTLE", "유리병");
        saveIfNotExists("VINYL", "비닐");
        saveIfNotExists("PAPER", "종이");
        saveIfNotExists("PAPER_PACK", "종이팩");
        saveIfNotExists("STYROFOAM", "스티로폼");
        // AI가 지원 품목으로 분류하지 못한 경우에도 정상적인 분석 결과로 저장한다.
        saveIfNotExists("UNKNOWN", "인식 불가");

        System.out.println("✅ 기초 마스터 데이터(품목)가 등록되었습니다!");
    }

    private void saveIfNotExists(String code, String name) {
        if (trashCategoryRepository.findByCode(code).isEmpty()) {
            trashCategoryRepository.save(
                    TrashCategory.builder()
                            .code(code)
                            .name(name)
                            .build()
            );
        }
    }
}
