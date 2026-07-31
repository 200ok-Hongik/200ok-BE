package com.team202ok.demo.recycle.controller;

import com.team202ok.demo.recycle.dto.AiReq;
import com.team202ok.demo.recycle.dto.AiRes;
import com.team202ok.demo.recycle.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

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
}