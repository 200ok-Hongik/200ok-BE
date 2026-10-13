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
    void registersConditionBasedClearPetBottleGuides() {
        TrashCategory category = TrashCategory.builder().code("CLEAR_PET_BOTTLE").name("무색 페트병").build();
        when(trashCategoryRepository.findByCode("CLEAR_PET_BOTTLE")).thenReturn(Optional.of(category));
        when(disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(any())).thenReturn(Optional.empty());

        initializer.run();

        ArgumentCaptor<ItemChecklist> checklists = ArgumentCaptor.forClass(ItemChecklist.class);
        verify(itemChecklistRepository, org.mockito.Mockito.times(6)).save(checklists.capture());
        assertThat(checklists.getAllValues())
                .extracting(ItemChecklist::getCheckItemName, ItemChecklist::getTriggerValue,
                        ItemChecklist::getGuideMessage, ItemChecklist::getDisplayOrder)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("isTransparent", "false", "투명 페트병인지 확인하세요", 1),
                        org.assertj.core.groups.Tuple.tuple("hasLabel", "true", "라벨을 제거하세요", 2),
                        org.assertj.core.groups.Tuple.tuple("isEmpty", "false", "내용물을 비우고 헹구세요", 3),
                        org.assertj.core.groups.Tuple.tuple("hasCap", "true", "뚜껑을 분리하세요", 4),
                        org.assertj.core.groups.Tuple.tuple("isContaminated", "true", "이물질을 제거하고 깨끗이 헹구세요", 5),
                        org.assertj.core.groups.Tuple.tuple("isCrushed", "false", "페트병을 압착하세요", 6));
        verify(disposalGuideRepository).save(any(DisposalGuide.class));
    }

    @Test
    void doesNotDuplicateExistingData() {
        TrashCategory category = TrashCategory.builder().code("CLEAR_PET_BOTTLE").name("무색 페트병").build();
        when(trashCategoryRepository.findByCode("CLEAR_PET_BOTTLE")).thenReturn(Optional.of(category));
        when(itemChecklistRepository.existsByTrashCategoryIdAndCheckItemName(any(), any())).thenReturn(true);
        when(disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(any()))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(DisposalGuide.class)));

        initializer.run();

        verify(itemChecklistRepository, never()).save(any());
        verify(disposalGuideRepository, never()).save(any());
    }
}
