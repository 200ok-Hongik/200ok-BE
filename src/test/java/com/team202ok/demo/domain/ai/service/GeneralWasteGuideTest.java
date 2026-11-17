package com.team202ok.demo.domain.ai.service;

import com.team202ok.demo.domain.ai.entity.ScanResult;
import com.team202ok.demo.domain.ai.repository.AiScanResultRepository;
import com.team202ok.demo.domain.ai.repository.ScanResultRepository;
import com.team202ok.demo.domain.disposal.entity.DisposalDecision;
import com.team202ok.demo.domain.disposal.entity.DisposalGuide;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.*;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneralWasteGuideTest {
    @Mock ScanResultRepository scans;
    @Mock AiScanResultRepository aiResults;
    @Mock DisposalDecisionRepository decisions;
    @Mock DisposalDecisionDetailRepository decisionDetails;
    @Mock TrashCategoryRepository categories;
    @Mock DisposalGuideRepository guides;
    @Mock ItemChecklistRepository checklists;
    @Mock UserRepository users;
    @InjectMocks AiAnalysisServiceImpl service;

    @Test
    void returnsBasicGuideForExistingDecisionWithoutChecklistSteps() {
        ScanResult scan = mock(ScanResult.class);
        DisposalDecision decision = mock(DisposalDecision.class);
        TrashCategory category = mock(TrashCategory.class);
        User user = mock(User.class);
        DisposalGuide guide = DisposalGuide.builder().trashCategoryId(3L)
                .guideMessage("종량제 봉투에 배출해 주세요.").cautionMessage("음식물쓰레기는 분리해 주세요.")
                .isActive(true).build();
        when(scans.findById(1L)).thenReturn(Optional.of(scan));
        when(scan.getUserId()).thenReturn(2L);
        when(decisions.findFirstByScanResultIdOrderByCreatedAtDesc(1L)).thenReturn(Optional.of(decision));
        when(decision.getAppliedCategoryId()).thenReturn(3L);
        when(decision.getScanResultId()).thenReturn(1L);
        when(decision.getIsPass()).thenReturn(true);
        when(decision.getGuideSnapshot()).thenReturn("");
        when(categories.findById(3L)).thenReturn(Optional.of(category));
        when(category.getId()).thenReturn(3L);
        when(guides.findByTrashCategoryIdAndIsActiveTrue(3L)).thenReturn(Optional.of(guide));
        when(users.findById(2L)).thenReturn(Optional.of(user));

        var result = service.getDisposalGuide(1L, 2L);

        assertThat(result.guideMessage()).contains("종량제 봉투");
        assertThat(result.cautionMessage()).contains("음식물쓰레기");
        assertThat(result.finalGuideMessage()).isEqualTo(result.guideMessage());
    }
}
