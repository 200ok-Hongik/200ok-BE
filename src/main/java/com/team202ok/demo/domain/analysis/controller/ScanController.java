package com.team202ok.demo.domain.analysis.controller;

import com.team202ok.demo.domain.analysis.dto.AiRes;
import com.team202ok.demo.domain.analysis.service.AiAnalysisService;
import com.team202ok.demo.domain.feedback.entity.UserFeedback;
import com.team202ok.demo.domain.feedback.repository.UserFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/scans")
@RequiredArgsConstructor
public class ScanController {
    private final AiAnalysisService aiAnalysisService;
    private final UserFeedbackRepository userFeedbackRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiRes.Analyze analyze(@RequestPart("image") MultipartFile image, @RequestParam Long userId) {
        return aiAnalysisService.analyze(image, userId);
    }

    @PostMapping("/{scanId}/feedback")
    public void createFeedback(@PathVariable Long scanId, @RequestParam Long userId,
                               @RequestBody FeedbackRequest request) {
        userFeedbackRepository.save(UserFeedback.builder()
                .scanResultId(scanId)
                .userCategoryId(null)
                .comment(request.comment())
                .build());
    }

    public record FeedbackRequest(String comment) {
    }
}
