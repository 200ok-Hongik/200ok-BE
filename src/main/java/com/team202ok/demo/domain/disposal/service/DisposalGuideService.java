package com.team202ok.demo.domain.disposal.service;

import com.team202ok.demo.domain.disposal.dto.DisposalGuideRes;
import java.util.List;

public interface DisposalGuideService {
    List<DisposalGuideRes.Category> getCategories();
    DisposalGuideRes.Detail getGuide(Long categoryId);
}
