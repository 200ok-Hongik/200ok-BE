package com.team202ok.demo.domain.analysis.service;

import com.team202ok.demo.domain.analysis.dto.AiReq;
import com.team202ok.demo.domain.analysis.dto.AiRes;
import org.springframework.web.multipart.MultipartFile;

public interface AiAnalysisService {

    AiRes.Analyze analyze(MultipartFile image, Long userId);

    AiRes.FinalGuide processFeedback(AiReq.Feedback request, Long userId);
}