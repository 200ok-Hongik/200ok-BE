package com.team202ok.demo.domain.ai.service;

import com.team202ok.demo.domain.ai.dto.AiReq;
import com.team202ok.demo.domain.ai.dto.AiRes;
import org.springframework.web.multipart.MultipartFile;

public interface AiAnalysisService {

    AiRes.Analyze analyze(MultipartFile image, Long userId);

    AiRes.FinalGuide processFeedback(AiReq.Feedback request, Long userId);

    AiRes.ScanDetail getScan(Long scanId, Long userId);

    AiRes.ConfirmedResult updateResult(Long scanId, AiReq.UpdateResult request, Long userId);

    AiRes.DisposalGuideDetail getDisposalGuide(Long scanId, Long userId);

    void createComment(Long scanId, String comment, Long userId);
}
