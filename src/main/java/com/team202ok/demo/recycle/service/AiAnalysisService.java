package com.team202ok.demo.recycle.service;

import com.team202ok.demo.recycle.dto.AiReq;
import com.team202ok.demo.recycle.dto.AiRes;
import org.springframework.web.multipart.MultipartFile;

public interface AiAnalysisService {

    AiRes.Analyze analyze(MultipartFile image, Long userId);

    AiRes.FinalGuide processFeedback(AiReq.Feedback request, Long userId);
}