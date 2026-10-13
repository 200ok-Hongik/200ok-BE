package com.team202ok.demo.domain.disposal.config;

import com.team202ok.demo.domain.disposal.entity.DisposalGuide;
import com.team202ok.demo.domain.disposal.entity.ItemChecklist;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.DisposalGuideRepository;
import com.team202ok.demo.domain.disposal.repository.ItemChecklistRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisposalDataInitializerTest {

    @Mock TrashCategoryRepository trashCategoryRepository;
    @Mock ItemChecklistRepository itemChecklistRepository;
    @Mock DisposalGuideRepository disposalGuideRepository;
    @InjectMocks DisposalDataInitializer initializer;

    @Test
    void registersConditionBasedGuidesForEverySupportedCategory() {
        AtomicLong ids = new AtomicLong(1);
        when(trashCategoryRepository.findByCode(any())).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            TrashCategory category = org.mockito.Mockito.mock(TrashCategory.class);
            when(category.getId()).thenReturn(ids.getAndIncrement());
            return Optional.of(category);
        });
        when(disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(any())).thenReturn(Optional.empty());

        initializer.run();

        ArgumentCaptor<ItemChecklist> checklists = ArgumentCaptor.forClass(ItemChecklist.class);
        verify(itemChecklistRepository, org.mockito.Mockito.times(34)).save(checklists.capture());
        assertThat(checklists.getAllValues())
                .extracting(ItemChecklist::getCheckItemName, ItemChecklist::getTriggerValue,
                        ItemChecklist::getGuideMessage, ItemChecklist::getDisplayOrder)
                .contains(
                        org.assertj.core.groups.Tuple.tuple("isTransparent", "false", "투명 페트병이 아니면 일반 플라스틱류로 분리배출하세요", 1),
                        org.assertj.core.groups.Tuple.tuple("hasLabel", "true", "라벨을 제거하세요", 2),
                        org.assertj.core.groups.Tuple.tuple("isEmpty", "false", "내용물을 비우고 헹구세요", 3),
                        org.assertj.core.groups.Tuple.tuple("isContaminated", "true", "이물질을 제거하고 깨끗이 헹구세요", 5),
                        org.assertj.core.groups.Tuple.tuple("isCrushed", "false", "페트병을 압착하세요", 6),
                        org.assertj.core.groups.Tuple.tuple("hasTape", "true", "테이프와 택배송장 등 종이가 아닌 재질을 제거하세요", 3),
                        org.assertj.core.groups.Tuple.tuple("hasStraw", "true", "빨대를 분리하세요", 2),
                        org.assertj.core.groups.Tuple.tuple("isFlattened", "false", "물기를 말린 뒤 펼치거나 압착하세요", 5),
                        org.assertj.core.groups.Tuple.tuple("hasLabel", "true", "상표를 제거하세요", 1));
        verify(disposalGuideRepository, org.mockito.Mockito.times(8)).save(any(DisposalGuide.class));
    }

    @Test
    void doesNotDuplicateExistingData() {
        TrashCategory category = org.mockito.Mockito.mock(TrashCategory.class);
        when(category.getId()).thenReturn(1L);
        when(trashCategoryRepository.findByCode(any())).thenReturn(Optional.of(category));
        ItemChecklist existingChecklist = org.mockito.Mockito.mock(ItemChecklist.class);
        when(existingChecklist.matchesDefinition(any(), any(), any())).thenReturn(true);
        when(itemChecklistRepository.findByTrashCategoryIdAndCheckItemName(any(), any()))
                .thenReturn(Optional.of(existingChecklist));
        when(disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(any()))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(DisposalGuide.class)));

        initializer.run();

        verify(itemChecklistRepository, never()).save(any());
        verify(disposalGuideRepository, never()).save(any());
    }
}
