package com.team202ok.demo.domain.analysis.service;

import com.team202ok.demo.domain.analysis.dto.AiModelResponse;
import com.team202ok.demo.domain.analysis.dto.AiReq;
import com.team202ok.demo.domain.analysis.dto.AiRes;
import com.team202ok.demo.domain.analysis.entity.*;
import com.team202ok.demo.domain.disposal.entity.*;
import com.team202ok.demo.domain.feedback.entity.*;
import com.team202ok.demo.domain.region.entity.RegionSchedule;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.global.exception.CategoryNotFoundException;
import com.team202ok.demo.global.exception.GeneralErrorCode;
import com.team202ok.demo.global.exception.ProjectException;
import com.team202ok.demo.global.exception.UserNotFoundException;
import com.team202ok.demo.domain.analysis.repository.*;
import com.team202ok.demo.domain.disposal.repository.*;
import com.team202ok.demo.domain.feedback.repository.*;
import com.team202ok.demo.domain.region.repository.RegionScheduleRepository;
import com.team202ok.demo.domain.user.repository.UserRepository;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
        AiModelResponse modelResponse = aiModelClient.requestAnalysis(image);

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
        getOwnedScan(request.scanResultId(), userId);
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

    @Override
    public AiRes.ScanDetail getScan(Long scanId, Long userId) {
        ScanResult scan = getOwnedScan(scanId, userId);
        AiScanResult ai = aiScanResultRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "AI 분석 결과를 찾을 수 없습니다."));
        TrashCategory category = trashCategoryRepository.findById(ai.getAiCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(String.valueOf(ai.getAiCategoryId())));
        AiRes.ScanDetail.UserResult userResult = disposalDecisionRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId)
                .map(d -> {
                    TrashCategory confirmedCategory = trashCategoryRepository.findById(d.getAppliedCategoryId())
                            .orElseThrow(() -> new CategoryNotFoundException(String.valueOf(d.getAppliedCategoryId())));
                    return AiRes.ScanDetail.UserResult.builder().decisionId(d.getId())
                            .category(category(confirmedCategory, null, d.getCategorySource()))
                            .isPass(d.getIsPass()).states(userStates(scanId, d.getAppliedCategoryId())).build();
                })
                .orElse(null);
        return AiRes.ScanDetail.builder().scanResultId(scan.getId()).imageUrl(scan.getImageUrl())
                .aiCategory(category(category, ai.getConfidence(), "AI")).aiStates(aiStates(ai, category.getId()))
                .confirmedResult(userResult).createdAt(scan.getCreatedAt()).build();
    }

    @Override
    public AiRes.ConfirmedResult updateResult(Long scanId, AiReq.UpdateResult request, Long userId) {
        getOwnedScan(scanId, userId);
        AiScanResult ai = aiScanResultRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "AI 분석 결과를 찾을 수 없습니다."));
        Long categoryId = request.categoryId() == null ? ai.getAiCategoryId() : request.categoryId();
        TrashCategory category = trashCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(String.valueOf(categoryId)));
        List<ItemChecklist> checklists = itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(categoryId);
        Map<Long, String> statuses = new HashMap<>();
        if (categoryId.equals(ai.getAiCategoryId())) {
            aiStates(ai, categoryId).stream()
                    .filter(state -> state.statusValue() != null)
                    .forEach(state -> statuses.put(state.checklistId(), state.statusValue()));
        }
        if (request.states() != null) {
            request.states().forEach(state -> statuses.put(state.checklistId(), state.statusValue()));
        }
        validateChecklistIds(statuses, checklists);
        UserFeedback feedback = userFeedbackRepository.save(UserFeedback.builder()
                .scanResultId(scanId).userCategoryId(categoryId).comment(request.comment()).build());
        statuses.forEach((id, value) -> userFeedbackDetailRepository.save(UserFeedbackDetail.builder()
                .userFeedbackId(feedback.getId()).checklistId(id).statusValue(value).build()));
        boolean isPass = checklists.stream().noneMatch(c -> c.isApplicable(statuses.get(c.getId())));
        List<String> steps = checklists.stream().filter(c -> c.isApplicable(statuses.get(c.getId()))).map(ItemChecklist::getGuideMessage).toList();
        String source = categoryId.equals(ai.getAiCategoryId()) ? "AI" : "USER";
        DisposalDecision decision = disposalDecisionRepository.save(DisposalDecision.builder().scanResultId(scanId)
                .appliedCategoryId(categoryId).categorySource(source).isPass(isPass).guideSnapshot(String.join(" / ", steps)).build());
        return AiRes.ConfirmedResult.builder().scanResultId(scanId).category(category(category, null, source))
                .states(userStates(feedback.getId(), checklists, statuses)).isConfirmed(true).decisionId(decision.getId()).build();
    }

    @Override
    public AiRes.DisposalGuideDetail getDisposalGuide(Long scanId, Long userId) {
        getOwnedScan(scanId, userId);
        DisposalDecision decision = disposalDecisionRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "확정된 분석 결과를 찾을 수 없습니다."));
        TrashCategory category = trashCategoryRepository.findById(decision.getAppliedCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(String.valueOf(decision.getAppliedCategoryId())));
        DisposalGuide guide = disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(category.getId()).orElse(null);
        Map<Long, String> statuses = latestStatusMap(scanId);
        List<AiRes.DisposalGuideDetail.GuideCheckItem> items = itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(category.getId()).stream()
                .map(c -> AiRes.DisposalGuideDetail.GuideCheckItem.builder().checklistId(c.getId()).checkItemName(c.getCheckItemName())
                        .statusValue(statuses.get(c.getId())).guideMessage(c.getGuideMessage())
                        .isSatisfied(!c.isApplicable(statuses.get(c.getId()))).build()).toList();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        AiRes.FinalGuide.ScheduleInfo schedule = user.getRegionCode() == null ? null : regionScheduleRepository.findByRegionCode(user.getRegionCode())
                .map(s -> AiRes.FinalGuide.ScheduleInfo.builder().dischargeDays(s.getDischargeDays()).dischargeTime(s.getDischargeTime()).build()).orElse(null);
        String message = decision.getGuideSnapshot();
        if (schedule != null) message += (message.isBlank() ? "" : " 조치한 후, ") + schedule.dischargeDays() + " " + schedule.dischargeTime() + "에 배출해 주세요.";
        return AiRes.DisposalGuideDetail.builder().decisionId(decision.getId()).scanResultId(scanId)
                .category(category(category, null, decision.getCategorySource())).isPass(decision.getIsPass())
                .guideMessage(guide == null ? "" : guide.getGuideMessage()).cautionMessage(guide == null ? null : guide.getCautionMessage())
                .checkItems(items).schedule(schedule).finalGuideMessage(message).build();
    }

    @Override
    public void createComment(Long scanId, String comment, Long userId) {
        getOwnedScan(scanId, userId);
        userFeedbackRepository.save(UserFeedback.builder()
                .scanResultId(scanId)
                .userCategoryId(null)
                .comment(comment)
                .build());
    }

    private ScanResult getOwnedScan(Long scanId, Long userId) {
        ScanResult scan = scanResultRepository.findById(scanId).orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "스캔 결과를 찾을 수 없습니다."));
        if (!scan.getUserId().equals(userId)) throw new ProjectException(GeneralErrorCode.FORBIDDEN);
        return scan;
    }

    private AiRes.ScanDetail.Category category(TrashCategory category, BigDecimal confidence, String source) {
        return AiRes.ScanDetail.Category.builder().categoryId(category.getId()).code(category.getCode()).name(category.getName())
                .confidence(confidence).categorySource(source).build();
    }

    private List<AiRes.Analyze.ChecklistResult> aiStates(AiScanResult ai, Long categoryId) {
        Map<Long, AiScanDetail> details = aiScanDetailRepository.findByAiScanResultId(ai.getId()).stream()
                .collect(Collectors.toMap(AiScanDetail::getChecklistId, Function.identity()));
        return itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(categoryId).stream().map(c -> {
            AiScanDetail d = details.get(c.getId());
            return AiRes.Analyze.ChecklistResult.builder().checklistId(c.getId()).checkItemName(c.getCheckItemName())
                    .statusValue(d == null ? null : d.getStatusValue()).confidence(d == null ? null : d.getConfidence()).build();
        }).toList();
    }

    private List<AiRes.Analyze.ChecklistResult> userStates(Long scanId, Long categoryId) {
        return userFeedbackRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId)
                .map(f -> userStates(f.getId(), itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(categoryId), latestStatusMap(scanId))).orElse(List.of());
    }

    private List<AiRes.Analyze.ChecklistResult> userStates(Long feedbackId, List<ItemChecklist> checklists, Map<Long, String> statuses) {
        return checklists.stream().map(c -> AiRes.Analyze.ChecklistResult.builder().checklistId(c.getId()).checkItemName(c.getCheckItemName())
                .statusValue(statuses.get(c.getId())).confidence(null).build()).toList();
    }

    private Map<Long, String> latestStatusMap(Long scanId) {
        return userFeedbackRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId).map(f -> userFeedbackDetailRepository.findByUserFeedbackId(f.getId()).stream()
                .collect(Collectors.toMap(UserFeedbackDetail::getChecklistId, UserFeedbackDetail::getStatusValue))).orElse(Map.of());
    }

    private void validateChecklistIds(Map<Long, String> statuses, List<ItemChecklist> checklists) {
        if (!checklists.stream().map(ItemChecklist::getId).collect(Collectors.toSet()).containsAll(statuses.keySet()))
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "선택한 품목의 체크리스트가 아닙니다.");
    }
}
