package com.team202ok.demo.recycle.service;

import com.team202ok.demo.recycle.dto.AiModelResponse;
import com.team202ok.demo.recycle.dto.AiReq;
import com.team202ok.demo.recycle.dto.AiRes;
import com.team202ok.demo.recycle.entity.*;
import com.team202ok.demo.recycle.exception.CategoryNotFoundException;
import com.team202ok.demo.recycle.exception.UserNotFoundException;
import com.team202ok.demo.recycle.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private final ImageUploader imageUploader;
    private final ScanResultRepository scanResultRepository;
    private final AiScanResultRepository aiScanResultRepository;
    private final AiScanDetailRepository aiScanDetailRepository;
    private final TrashCategoryRepository trashCategoryRepository;
    private final ItemChecklistRepository itemChecklistRepository;
    private final AiModelClient aiModelClient;
    private final UserRepository userRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserFeedbackDetailRepository userFeedbackDetailRepository;
    private final DisposalDecisionRepository disposalDecisionRepository;
    private final DisposalGuideRepository disposalGuideRepository;
    private final RegionScheduleRepository regionScheduleRepository;

    @Override
    public AiRes.Analyze analyze(MultipartFile image, Long userId) {

        // 1. 이미지 업로드
        String imageUrl = imageUploader.upload(image);

        // 2. scan_results 저장
        ScanResult scanResult = scanResultRepository.save(
                ScanResult.builder()
                        .userId(userId)
                        .imageUrl(imageUrl)
                        .build()
        );

        // 3. AI 서버 호출
        AiModelResponse modelResponse = aiModelClient.requestAnalysis(imageUrl);

        // 4. categoryCode -> TrashCategory 조회
        TrashCategory category = trashCategoryRepository.findByCode(modelResponse.getCategoryCode())
                .orElseThrow(() -> new CategoryNotFoundException(modelResponse.getCategoryCode()));

        // 5. ai_scan_results 저장
        AiScanResult aiScanResult = aiScanResultRepository.save(
                AiScanResult.builder()
                        .scanResultId(scanResult.getId())
                        .aiCategoryId(category.getId())
                        .confidence(modelResponse.getCategoryConfidence())
                        .modelVersion(modelResponse.getModelVersion())
                        .rawResponse(modelResponse.getRawJson())
                        .build()
        );

        // 6. 해당 품목의 체크리스트 조회 (display_order 기준 정렬)
        List<ItemChecklist> checklists =
                itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(category.getId());

        // 7. checkItemName 기준 매핑 후 저장 + 응답 구성
        List<AiRes.Analyze.ChecklistResult> results = new ArrayList<>();

        for (ItemChecklist checklist : checklists) {
            AiModelResponse.CheckItem item =
                    modelResponse.getCheckItemByName(checklist.getCheckItemName());

            String statusValue = item != null ? item.getStatusValue() : null;
            BigDecimal confidence = item != null ? item.getConfidence() : null;

            if (statusValue != null) {
                aiScanDetailRepository.save(
                        AiScanDetail.builder()
                                .aiScanResultId(aiScanResult.getId())
                                .checklistId(checklist.getId())
                                .statusValue(statusValue)
                                .confidence(confidence)
                                .build()
                );
            }

            results.add(AiRes.Analyze.ChecklistResult.builder()
                    .checklistId(checklist.getId())
                    .checkItemName(checklist.getCheckItemName())
                    .statusValue(statusValue)
                    .confidence(confidence)
                    .build());
        }

        return AiRes.Analyze.builder()
                .scanResultId(scanResult.getId())
                .categoryCode(category.getCode())
                .categoryConfidence(modelResponse.getCategoryConfidence())
                .modelVersion(modelResponse.getModelVersion())
                .checklistResults(results)
                .build();
    }

    @Override
    public AiRes.FinalGuide processFeedback(AiReq.Feedback request, Long userId) {

        // 1. 기초 데이터 조회
        TrashCategory category = trashCategoryRepository.findByCode(request.categoryCode())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryCode()));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 2. user_feedbacks 저장
        UserFeedback userFeedback = userFeedbackRepository.save(
                UserFeedback.builder()
                        .scanResultId(request.scanResultId())
                        .userCategoryId(category.getId())
                        .build()
        );

        // checklistId -> statusValue 매핑 (필터링/판정에 사용)
        Map<Long, String> statusMap = new HashMap<>();
        for (AiReq.Feedback.ChecklistFeedback fb : request.checklistFeedbacks()) {
            userFeedbackDetailRepository.save(
                    UserFeedbackDetail.builder()
                            .userFeedbackId(userFeedback.getId())
                            .checklistId(fb.checklistId())
                            .statusValue(fb.statusValue())
                            .build()
            );
            statusMap.put(fb.checklistId(), fb.statusValue());
        }

        // 3. 해당 품목 체크리스트 전체 조회 (display_order 순)
        List<ItemChecklist> checklists =
                itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(category.getId());

        // isApplicable == true 인 항목이 하나라도 남아있으면 "아직 조치 필요" -> isPass = false
        boolean isPass = checklists.stream()
                .noneMatch(c -> c.isApplicable(statusMap.get(c.getId())));

        // 실제로 안내가 필요한 스텝만 순서대로 추출
        List<String> steps = checklists.stream()
                .filter(c -> c.isApplicable(statusMap.get(c.getId())))
                .sorted(Comparator.comparing(ItemChecklist::getDisplayOrder))
                .map(ItemChecklist::getGuideMessage)
                .toList();

        // 4. disposal_decisions 저장
        disposalDecisionRepository.save(
                DisposalDecision.builder()
                        .scanResultId(request.scanResultId())
                        .appliedCategoryId(category.getId())
                        .categorySource("USER")
                        .isPass(isPass)
                        .guideSnapshot(String.join(" / ", steps))
                        .build()
        );

        // 5. disposal_guides 조회 (품목 공통 안내)
        String guideMessage = disposalGuideRepository
                .findByTrashCategoryIdAndIsActiveTrue(category.getId())
                .map(DisposalGuide::getGuideMessage)
                .orElse("");

        // 6. region_schedules 조회 (regionCode 1건 매칭)
        AiRes.FinalGuide.ScheduleInfo schedule = null;
        if (user.getRegionCode() != null) {
            schedule = regionScheduleRepository.findByRegionCode(user.getRegionCode())
                    .map(rs -> AiRes.FinalGuide.ScheduleInfo.builder()
                            .dischargeDays(rs.getDischargeDays())
                            .dischargeTime(rs.getDischargeTime())
                            .build())
                    .orElse(null);
        }

        // 7. 최종 응답 조립
        return AiRes.FinalGuide.builder()
                .isPass(isPass)
                .categoryName(category.getName())
                .guideMessage(guideMessage)
                .steps(steps)
                .schedule(schedule)
                .build();
    }
}