package com.team202ok.demo.domain.disposal.config;

import com.team202ok.demo.domain.disposal.entity.DisposalGuide;
import com.team202ok.demo.domain.disposal.entity.ItemChecklist;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.DisposalGuideRepository;
import com.team202ok.demo.domain.disposal.repository.ItemChecklistRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 상태값을 실제 분리배출 조치로 변환하기 위한 기준 데이터를 등록한다.
 * 상태 키는 AI 응답의 finalResult.states 키와 정확히 일치해야 한다.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class DisposalDataInitializer implements CommandLineRunner {

    private static final List<ChecklistSeed> CLEAR_PET_BOTTLE_CHECKLISTS = List.of(
            new ChecklistSeed("isTransparent", "투명 페트병인지 확인하세요", 1, "false"),
            new ChecklistSeed("hasLabel", "라벨을 제거하세요", 2, "true"),
            new ChecklistSeed("isEmpty", "내용물을 비우고 헹구세요", 3, "false"),
            new ChecklistSeed("hasCap", "뚜껑을 분리하세요", 4, "true"),
            new ChecklistSeed("isContaminated", "이물질을 제거하고 깨끗이 헹구세요", 5, "true"),
            new ChecklistSeed("isCrushed", "페트병을 압착하세요", 6, "false")
    );

    private final TrashCategoryRepository trashCategoryRepository;
    private final ItemChecklistRepository itemChecklistRepository;
    private final DisposalGuideRepository disposalGuideRepository;

    @Override
    public void run(String... args) {
        TrashCategory category = trashCategoryRepository.findByCode("CLEAR_PET_BOTTLE")
                .orElseThrow(() -> new IllegalStateException("CLEAR_PET_BOTTLE 카테고리가 등록되지 않았습니다."));

        CLEAR_PET_BOTTLE_CHECKLISTS.forEach(seed -> saveChecklistIfMissing(category.getId(), seed));
        saveGuideIfMissing(category.getId());
    }

    private void saveChecklistIfMissing(Long categoryId, ChecklistSeed seed) {
        if (itemChecklistRepository.existsByTrashCategoryIdAndCheckItemName(categoryId, seed.stateKey())) {
            return;
        }
        itemChecklistRepository.save(ItemChecklist.builder()
                .trashCategoryId(categoryId)
                .checkItemName(seed.stateKey())
                .guideMessage(seed.guideMessage())
                .displayOrder(seed.displayOrder())
                .triggerValue(seed.triggerValue())
                .build());
    }

    private void saveGuideIfMissing(Long categoryId) {
        if (disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(categoryId).isPresent()) {
            return;
        }
        disposalGuideRepository.save(DisposalGuide.builder()
                .trashCategoryId(categoryId)
                .guideMessage("페트병은 상태에 따라 필요한 조치를 완료한 뒤 압착하여 배출해 주세요.")
                .cautionMessage("내용물이나 이물질이 남아 있으면 재활용이 어렵습니다.")
                .isActive(true)
                .build());
    }

    private record ChecklistSeed(String stateKey, String guideMessage, Integer displayOrder,
                                 String triggerValue) {
    }
}
