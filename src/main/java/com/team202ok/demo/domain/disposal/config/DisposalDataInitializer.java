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

    private static final List<CategorySeed> CATEGORY_SEEDS = List.of(
            new CategorySeed("CLEAR_PET_BOTTLE",
                    "상태에 따라 필요한 조치를 완료한 뒤 투명 페트병 전용 수거함에 배출해 주세요.",
                    "내용물이나 이물질이 남아 있으면 재활용이 어렵습니다.",
                    List.of(
                            step("isTransparent", "투명 페트병이 아니면 일반 플라스틱류로 분리배출하세요", 1, "false"),
                            step("hasLabel", "라벨을 제거하세요", 2, "true"),
                            step("isEmpty", "내용물을 비우고 헹구세요", 3, "false"),
                            step("hasCap", "뚜껑을 닫아 배출하세요", 4, "false"),
                            step("isContaminated", "이물질을 제거하고 깨끗이 헹구세요", 5, "true"),
                            step("isCrushed", "페트병을 압착하세요", 6, "false")
                    )),
            new CategorySeed("PLASTIC",
                    "내용물을 비우고 다른 재질을 제거한 뒤 플라스틱류로 배출해 주세요.",
                    "씻어도 이물질이 제거되지 않는 용기는 종량제 봉투로 배출하세요.",
                    List.of(
                            step("hasLabel", "라벨을 제거하세요", 1, "true"),
                            step("hasCap", "뚜껑 등 다른 재질을 분리하세요", 2, "true"),
                            step("isEmpty", "내용물을 완전히 비우세요", 3, "false"),
                            step("isContaminated", "이물질과 물기를 제거하고 깨끗이 헹구세요", 4, "true"),
                            step("isCrushed", "부피를 줄일 수 있도록 압착하세요", 5, "false"),
                            step("isTransparent", "투명 페트병은 투명 페트병 전용 수거함에 배출하세요", 6, "true")
                    )),
            new CategorySeed("CAN",
                    "내용물을 비우고 이물질을 제거한 뒤 캔류로 배출해 주세요.",
                    "가스 용기는 통풍이 잘되는 곳에서 내용물을 완전히 제거한 뒤 배출하세요.",
                    List.of(
                            step("isEmpty", "내용물을 완전히 비우세요", 1, "false"),
                            step("isContaminated", "이물질을 제거하고 물로 헹구세요", 2, "true"),
                            step("isCrushed", "부피를 줄일 수 있도록 압착하세요", 3, "false")
                    )),
            new CategorySeed("GLASS_BOTTLE",
                    "내용물을 비우고 이물질을 제거한 뒤 유리병류로 배출해 주세요.",
                    "깨진 유리나 내열유리, 도자기는 유리병류로 배출하면 안 됩니다.",
                    List.of(
                            step("isEmpty", "내용물을 완전히 비우세요", 1, "false"),
                            step("isContaminated", "이물질을 제거하고 물로 헹구세요", 2, "true"),
                            step("hasCap", "금속이나 플라스틱 뚜껑을 분리하세요", 3, "true"),
                            step("hasLabel", "쉽게 떼어지는 라벨은 제거하세요", 4, "true"),
                            step("isBroken", "깨진 유리는 신문지 등으로 감싸 지자체 기준에 따라 배출하세요", 5, "true")
                    )),
            new CategorySeed("VINYL",
                    "이물질을 제거한 뒤 비닐류끼리 모아 배출해 주세요.",
                    "음식물 등 이물질이 제거되지 않는 비닐은 종량제 봉투로 배출하세요.",
                    List.of(
                            step("isEmpty", "내용물을 완전히 비우세요", 1, "false"),
                            step("isContaminated", "내용물과 이물질을 제거하고 깨끗이 씻으세요", 2, "true")
                    )),
            new CategorySeed("PAPER",
                    "종이가 아닌 재질을 제거하고 펼치거나 접어서 종이류로 배출해 주세요.",
                    "젖거나 음식물·기름 등으로 오염된 종이는 종량제 봉투로 배출하세요.",
                    List.of(
                            step("isContaminated", "음식물과 기름 등 이물질이 묻은 부분을 제거하세요", 1, "true"),
                            step("isWet", "젖은 종이는 충분히 말리세요", 2, "true"),
                            step("hasTape", "테이프와 택배송장 등 종이가 아닌 재질을 제거하세요", 3, "true"),
                            step("isFlattened", "상자와 종이를 펼치거나 접어 부피를 줄이세요", 4, "false")
                    )),
            new CategorySeed("PAPER_PACK",
                    "내용물을 비우고 헹군 뒤 말려서 종이팩 전용 수거함에 배출해 주세요.",
                    "일반 종이류와 섞지 말고 종이팩 전용 수거함을 이용하세요.",
                    List.of(
                            step("hasCap", "뚜껑을 분리하세요", 1, "true"),
                            step("hasStraw", "빨대를 분리하세요", 2, "true"),
                            step("isEmpty", "내용물을 완전히 비우세요", 3, "false"),
                            step("isContaminated", "물로 헹구어 이물질을 제거하세요", 4, "true"),
                            step("isFlattened", "물기를 말린 뒤 펼치거나 압착하세요", 5, "false")
                    )),
            new CategorySeed("STYROFOAM",
                    "내용물과 부착물을 제거한 뒤 깨끗한 흰색 스티로폼만 분리배출해 주세요.",
                    "음식물이 묻었거나 코팅·착색된 스티로폼은 종량제 봉투로 배출하세요.",
                    List.of(
                            step("hasLabel", "상표를 제거하세요", 1, "true"),
                            step("hasTape", "테이프 등 부착물을 제거하세요", 2, "true"),
                            step("isContaminated", "내용물과 이물질을 완전히 제거하세요", 3, "true")
                    ))
    );

    private final TrashCategoryRepository trashCategoryRepository;
    private final ItemChecklistRepository itemChecklistRepository;
    private final DisposalGuideRepository disposalGuideRepository;

    @Override
    public void run(String... args) {
        CATEGORY_SEEDS.forEach(this::saveCategoryData);
    }

    private void saveCategoryData(CategorySeed seed) {
        TrashCategory category = trashCategoryRepository.findByCode(seed.categoryCode())
                .orElseThrow(() -> new IllegalStateException(seed.categoryCode() + " 카테고리가 등록되지 않았습니다."));
        seed.checklists().forEach(checklist -> saveChecklistIfMissing(category.getId(), checklist));
        saveGuideIfMissing(category.getId(), seed);
    }

    private void saveChecklistIfMissing(Long categoryId, ChecklistSeed seed) {
        var existing = itemChecklistRepository.findByTrashCategoryIdAndCheckItemName(categoryId, seed.stateKey());
        if (existing.isPresent()) {
            ItemChecklist checklist = existing.get();
            if (!checklist.matchesDefinition(seed.guideMessage(), seed.displayOrder(), seed.triggerValue())) {
                checklist.updateDefinition(seed.guideMessage(), seed.displayOrder(), seed.triggerValue());
                itemChecklistRepository.save(checklist);
            }
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

    private void saveGuideIfMissing(Long categoryId, CategorySeed seed) {
        if (disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(categoryId).isPresent()) {
            return;
        }
        disposalGuideRepository.save(DisposalGuide.builder()
                .trashCategoryId(categoryId)
                .guideMessage(seed.guideMessage())
                .cautionMessage(seed.cautionMessage())
                .isActive(true)
                .build());
    }

    private static ChecklistSeed step(String stateKey, String guideMessage, int displayOrder,
                                      String triggerValue) {
        return new ChecklistSeed(stateKey, guideMessage, displayOrder, triggerValue);
    }

    private record CategorySeed(String categoryCode, String guideMessage, String cautionMessage,
                                List<ChecklistSeed> checklists) {
    }

    private record ChecklistSeed(String stateKey, String guideMessage, Integer displayOrder,
                                 String triggerValue) {
    }
}
