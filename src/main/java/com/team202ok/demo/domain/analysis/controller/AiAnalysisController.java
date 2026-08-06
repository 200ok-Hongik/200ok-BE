package com.team202ok.demo.domain.analysis.controller;

import com.team202ok.demo.domain.analysis.dto.AiReq;
import com.team202ok.demo.domain.analysis.dto.AiRes;
import com.team202ok.demo.domain.analysis.service.AiAnalysisService;
import com.team202ok.demo.domain.analysis.service.AiModelClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;
    private final AiModelClient aiModelClient;

    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // 변경: ResponseEntity<AiRes> -> ResponseEntity<AiRes.Analyze>
    public ResponseEntity<AiRes.Analyze> analyze(
            @RequestPart("image") MultipartFile image,
            @RequestParam Long userId // TODO: 인증 붙으면 @AuthenticationPrincipal로 교체
    ) {
        return ResponseEntity.ok(
                aiAnalysisService.analyze(image, userId)
        );
    }

    @PostMapping("/feedback")
    // 변경: ResponseEntity<?> -> ResponseEntity<AiRes.FinalGuide>
    public ResponseEntity<AiRes.FinalGuide> submitFeedback(
            @RequestBody AiReq.Feedback request, // 주석 해제 및 AiReq.Feedback으로 타입 지정
            @RequestParam Long userId
    ) {
        // 서비스의 processFeedback 메서드를 호출하고 그 결과를 바로 리턴합니다.
        return ResponseEntity.ok(
                aiAnalysisService.processFeedback(request, userId)
        );
    }

    @GetMapping("/server/health")
    public ResponseEntity<String> checkAiServerHealth() {
        boolean isHealthy = aiModelClient.checkServerHealth();
        return isHealthy
                ? ResponseEntity.ok("AI 서버 정상")
                : ResponseEntity.status(503).body("AI 서버 응답 없음");
    }


    @GetMapping("/ai-health")
    public ResponseEntity<String> testAiServerHealth() {
        return checkAiServerHealth();
    }
}
