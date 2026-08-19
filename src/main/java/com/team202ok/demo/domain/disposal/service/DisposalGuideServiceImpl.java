package com.team202ok.demo.domain.disposal.service;

import com.team202ok.demo.domain.disposal.dto.DisposalGuideRes;
import com.team202ok.demo.domain.disposal.entity.DisposalGuide;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.DisposalGuideRepository;
import com.team202ok.demo.domain.disposal.repository.ItemChecklistRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import com.team202ok.demo.global.exception.GeneralErrorCode;
import com.team202ok.demo.global.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisposalGuideServiceImpl implements DisposalGuideService {
    private final TrashCategoryRepository trashCategoryRepository;
    private final DisposalGuideRepository disposalGuideRepository;
    private final ItemChecklistRepository itemChecklistRepository;

    @Override
    public List<DisposalGuideRes.Category> getCategories() {
        return trashCategoryRepository.findAll().stream().map(this::category).toList();
    }

    @Override
    public DisposalGuideRes.Detail getGuide(Long categoryId) {
        TrashCategory category = trashCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "쓰레기 품목을 찾을 수 없습니다."));
        DisposalGuide guide = disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(categoryId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "품목별 안내를 찾을 수 없습니다."));
        return new DisposalGuideRes.Detail(category(category), guide.getGuideMessage(), guide.getCautionMessage(),
                itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(categoryId).stream()
                        .map(item -> new DisposalGuideRes.CheckItem(item.getId(), item.getCheckItemName(),
                                item.getGuideMessage(), item.getDisplayOrder())).toList());
    }

    private DisposalGuideRes.Category category(TrashCategory category) {
        return new DisposalGuideRes.Category(category.getId(), category.getCode(), category.getName());
    }
}
