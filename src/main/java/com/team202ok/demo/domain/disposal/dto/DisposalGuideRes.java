package com.team202ok.demo.domain.disposal.dto;

import java.util.List;

public final class DisposalGuideRes {
    private DisposalGuideRes() {}

    public record Category(Long categoryId, String code, String name) {}
    public record Detail(Category category, String guideMessage, String cautionMessage, List<CheckItem> checkItems) {}
    public record CheckItem(Long checklistId, String checkItemName, String guideMessage, Integer displayOrder) {}
}
