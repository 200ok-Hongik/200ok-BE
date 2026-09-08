package com.team202ok.demo.domain.ai.controller;

import com.team202ok.demo.domain.ai.dto.AiReq;
import com.team202ok.demo.domain.ai.dto.AiRes;
import com.team202ok.demo.domain.ai.service.AiAnalysisService;
import com.team202ok.demo.domain.ai.service.AiModelClient;
import com.team202ok.demo.domain.rabbitmq.AnalysisJobService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "이미지 비동기 분석 접수 및 결과 조회 API입니다.")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;
    private final AiModelClient aiModelClient;
    private final AnalysisJobService analysisJobs;

    @PostMapping(value = "/analysis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 분석 접수", description = "작업을 저장하고 즉시 202와 jobId를 반환합니다. 완료 결과는 jobId로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "분석 작업 접수됨")
    public ResponseEntity<AnalysisJobService.JobView> analyze(
            @RequestPart("image") MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    ) throws IOException {
        var job = analysisJobs.submit(image, userId);
        return ResponseEntity.accepted()
                .location(java.net.URI.create("/api/ai/analysis/" + job.jobId())).body(job);
    }

    @GetMapping("/analysis/{jobId}")
    @Operation(summary = "분석 작업 결과 조회", description = "본인 작업의 상태 및 완료된 객체별 결과를 반환합니다.")
    public AnalysisJobService.JobView getAnalysis(@PathVariable String jobId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) throws IOException {
        return analysisJobs.get(jobId, userId);
    }

    @PostMapping("/feedback")
    @Operation(summary = "분석 결과 확정 (기존 API)", description = "사용자가 수정한 품목과 체크 상태로 분리배출 판단을 생성합니다.")
    // 변경: ResponseEntity<?> -> ResponseEntity<AiRes.FinalGuide>
    public ResponseEntity<AiRes.FinalGuide> submitFeedback(
            @RequestBody AiReq.Feedback request, // 주석 해제 및 AiReq.Feedback으로 타입 지정
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
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


//    @GetMapping("/ai-health")
//    @Operation(summary = "AI 서버 상태 확인 (별칭)", description = "/server/health와 동일한 호환용 엔드포인트입니다.")
//    public ResponseEntity<String> testAiServerHealth() {
//        return checkAiServerHealth();
//    }
}
