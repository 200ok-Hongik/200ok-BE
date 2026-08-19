package com.team202ok.demo.domain.ai.dto;

import java.util.List;

public final class AiReq {

    private AiReq() {
    }

    // ==========================================
    // [STEP 1] AI 분석 요청
    // ==========================================
    public record Analyze(
            // 현재 컨트롤러에서 MultipartFile과 RequestParam으로 받고 있으므로 비어있을 수 있습니다.
            // 추후 JSON으로 받을 데이터가 생기면 여기에 추가합니다.
    ) {
    }

    // ==========================================
    // [STEP 2] 사용자 피드백 제출 요청
    // ==========================================
    public record Feedback(
            Long scanResultId,
            String categoryCode, // 💡 수정됨: Long categoryId -> String categoryCode
            List<ChecklistFeedback> checklistFeedbacks
    ) {
        public record ChecklistFeedback(
                Long checklistId,
                String statusValue
        ) {}
    }

    public record UpdateResult(Long categoryId, List<ChecklistFeedback> states, String comment) {
        public record ChecklistFeedback(Long checklistId, String statusValue) {}
    }
}
