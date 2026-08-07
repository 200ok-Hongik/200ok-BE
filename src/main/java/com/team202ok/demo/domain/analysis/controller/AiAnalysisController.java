package com.team202ok.demo.domain.analysis.controller;

import com.team202ok.demo.domain.analysis.dto.AiReq;
import com.team202ok.demo.domain.analysis.dto.AiRes;
import com.team202ok.demo.domain.analysis.service.AiAnalysisService;
import com.team202ok.demo.domain.analysis.service.AiModelClient;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI (호환 API)", description = "기존 AI 분석 및 AI 서버 상태 확인 API입니다. 신규 스캔 API는 /api/scans를 사용합니다.")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;
    private final AiModelClient aiModelClient;

    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 AI 분석 (기존 API)", description = "업로드한 이미지 파일을 AI 서버에 전달해 품목과 상태를 분석합니다.")
    // 변경: ResponseEntity<AiRes> -> ResponseEntity<AiRes.Analyze>
    public ResponseEntity<AiRes.Analyze> analyze(
            @RequestPart("image") MultipartFile image,
            @Parameter(description = "임시 사용자 ID (카카오 로그인 연동 전)", example = "1") @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                aiAnalysisService.analyze(image, userId)
        );
    }

    @PostMapping("/feedback")
    @Operation(summary = "분석 결과 확정 (기존 API)", description = "사용자가 수정한 품목과 체크 상태로 분리배출 판단을 생성합니다.")
    // 변경: ResponseEntity<?> -> ResponseEntity<AiRes.FinalGuide>
    public ResponseEntity<AiRes.FinalGuide> submitFeedback(
            @RequestBody AiReq.Feedback request, // 주석 해제 및 AiReq.Feedback으로 타입 지정
            @Parameter(description = "임시 사용자 ID (카카오 로그인 연동 전)", example = "1") @RequestParam Long userId
    ) {
        // 서비스의 processFeedback 메서드를 호출하고 그 결과를 바로 리턴합니다.
        return ResponseEntity.ok(
                aiAnalysisService.processFeedback(request, userId)
        );
    }

    @GetMapping("/server/health")
    @Operation(summary = "AI 서버 상태 확인", description = "설정된 AI 서버의 /health 엔드포인트 연결 상태를 확인합니다.")
    public ResponseEntity<String> checkAiServerHealth() {
        boolean isHealthy = aiModelClient.checkServerHealth();
        return isHealthy
                ? ResponseEntity.ok("AI 서버 정상")
                : ResponseEntity.status(503).body("AI 서버 응답 없음");
    }


    @GetMapping("/ai-health")
    @Operation(summary = "AI 서버 상태 확인 (별칭)", description = "/server/health와 동일한 호환용 엔드포인트입니다.")
    public ResponseEntity<String> testAiServerHealth() {
        return checkAiServerHealth();
    }
}
