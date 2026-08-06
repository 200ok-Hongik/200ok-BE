package com.team202ok.demo.domain.disposal.config;

import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


//추후 품목 완전 확정시 data.sql로 변환!!!!
@Component
@Order(0)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TrashCategoryRepository trashCategoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // 서버가 켜질 때 한 번 실행되는 곳입니다.

        // 데이터가 비어있을 때만 기초 데이터를 넣습니다. (중복 저장 방지)
        if (trashCategoryRepository.count() == 0) {
            TrashCategory petBottle = TrashCategory.builder()
                    .code("PET_BOTTLE")
                    .name("페트병")
                    .build();

            TrashCategory can = TrashCategory.builder()
                    .code("CAN")
                    .name("캔류")
                    .build();

            trashCategoryRepository.save(petBottle);
            trashCategoryRepository.save(can);

            System.out.println("✅ 기초 마스터 데이터(품목)가 성공적으로 등록되었습니다!");
        }
    }
}