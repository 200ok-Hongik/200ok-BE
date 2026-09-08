package com.team202ok.demo.domain.ai.service;

import com.team202ok.demo.domain.ai.dto.AiModelResponse;
import com.team202ok.demo.domain.ai.dto.AiReq;
import com.team202ok.demo.domain.ai.dto.AiRes;
import com.team202ok.demo.domain.ai.entity.*;
import com.team202ok.demo.domain.disposal.entity.*;
import com.team202ok.demo.domain.feedback.entity.*;
import com.team202ok.demo.domain.region.entity.RegionSchedule;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.global.exception.code.GeneralErrorCode;
import com.team202ok.demo.global.exception.custom.CategoryNotFoundException;
import com.team202ok.demo.global.exception.custom.ProjectException;
import com.team202ok.demo.global.exception.custom.UserNotFoundException;
import com.team202ok.demo.domain.ai.repository.*;
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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;
    private final ScanResultRepository scanResultRepository;
    private final AiScanResultRepository aiScanResultRepository;
    private final AiScanDetailRepository aiScanDetailRepository;
    private final TrashCategoryRepository trashCategoryRepository;
    private final ItemChecklistRepository itemChecklistRepository;
    private final AiModelClient aiModelClient;
    private final UserRepository userRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserFeedbackDetailRepository userFeedbackDetailRepository;
    private final ScanCommentRepository scanCommentRepository;
    private final DisposalDecisionRepository disposalDecisionRepository;
    private final DisposalDecisionDetailRepository disposalDecisionDetailRepository;
    private final DisposalGuideRepository disposalGuideRepository;
    private final RegionScheduleRepository regionScheduleRepository;

    @Override
    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public AiRes.Analyze analyze(MultipartFile image, Long userId) {

        AiModelResponse response = aiModelClient.requestAnalysis(image);
        response.validate();
        // Resolve every category before writing anything, including multi-object responses.
        Map<String, TrashCategory> categories = new HashMap<>();
        for (AiModelResponse.DetectedObject object : response.getObjects()) {
            String code = object.finalResult().itemCode();
            categories.computeIfAbsent(code, key -> trashCategoryRepository.findByCode(key)
                    .orElseThrow(() -> new CategoryNotFoundException(key)));
        }
        String imageUrl = imageUploader.upload(image);
        // The network calls stay outside the transaction; all database writes are atomic.
        return transactionTemplate.execute(status -> {
            ScanResult scan = scanResultRepository.save(ScanResult.builder()
                    .userId(userId).imageUrl(imageUrl).aiRawResponse(response.getRawJson()).build());
            for (AiModelResponse.DetectedObject object : response.getObjects()) {
                TrashCategory category = categories.get(object.finalResult().itemCode());
                String rawObject;
                try {
                    rawObject = objectMapper.writeValueAsString(object);
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new IllegalStateException("AI 객체 저장 실패", e);
                }
                AiScanResult ai = aiScanResultRepository.save(AiScanResult.builder()
                        .scanResultId(scan.getId()).objectId(object.objectId())
                        .aiCategoryId(category.getId()).rawResponse(rawObject).build());
                // Compatibility projection for existing checklist-based readers only.
                // Full typed states, bbox and source are retained in rawResponse.
                for (ItemChecklist checklist : itemChecklistRepository
                        .findByTrashCategoryIdOrderByDisplayOrder(category.getId())) {
                    com.fasterxml.jackson.databind.JsonNode value = object.finalResult().states()
                            .get(checklist.getCheckItemName());
                    if (value != null && !value.isNull() && value.isValueNode()) {
                        aiScanDetailRepository.save(AiScanDetail.builder().aiScanResultId(ai.getId())
                                .checklistId(checklist.getId()).statusValue(value.asText()).build());
                    }
                }
            }
            return AiRes.Analyze.builder().scanResultId(scan.getId())
                    .objects(response.getObjects()).additionalObjects(response.getAdditionalObjects()).build();
        });
    }

    @Override
    public AiRes.FinalGuide processFeedback(AiReq.Feedback request, Long userId) {
        TrashCategory category = trashCategoryRepository.findByCode(request.categoryCode())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryCode()));
        List<AiReq.UpdateResult.ChecklistFeedback> states = request.checklistFeedbacks().stream()
                .map(state -> new AiReq.UpdateResult.ChecklistFeedback(state.checklistId(), state.statusValue()))
                .toList();
        AiRes.ConfirmedResult confirmed = updateResult(request.scanResultId(),
                new AiReq.UpdateResult(category.getId(), states, null), userId);
        AiRes.DisposalGuideDetail guide = getDisposalGuide(request.scanResultId(), userId);
        List<String> steps = guide.checkItems().stream()
                .filter(item -> !item.isSatisfied())
                .map(AiRes.DisposalGuideDetail.GuideCheckItem::guideMessage)
                .toList();
        return AiRes.FinalGuide.builder()
                .scanResultId(request.scanResultId())
                .decisionId(confirmed.decisionId())
                .isPass(confirmed.isConfirmed() && guide.isPass())
                .categoryName(category.getName())
                .guideMessage(guide.guideMessage())
                .steps(steps)
                .schedule(guide.schedule())
                .build();
    }

    @Override
    public AiRes.ScanDetail getScan(Long scanId, Long userId) {
        ScanResult scan = getOwnedScan(scanId, userId);
        requireLegacySingleObject(scan);
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
                            .isPass(d.getIsPass()).states(decisionStates(d, confirmedCategory)).build();
                })
                .orElse(null);
        return AiRes.ScanDetail.builder().scanResultId(scan.getId()).imageUrl(scan.getImageUrl())
                .aiCategory(category(category, ai.getConfidence(), "AI")).aiStates(aiStates(ai, category.getId()))
                .confirmedResult(userResult).createdAt(scan.getCreatedAt()).build();
    }

    @Override
    public AiRes.ConfirmedResult updateResult(Long scanId, AiReq.UpdateResult request, Long userId) {
        requireLegacySingleObject(getOwnedScan(scanId, userId));
        AiScanResult ai = aiScanResultRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "AI 분석 결과를 찾을 수 없습니다."));
        Long categoryId = request.categoryId() == null ? ai.getAiCategoryId() : request.categoryId();
        TrashCategory category = trashCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(String.valueOf(categoryId)));
        List<ItemChecklist> checklists = itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(categoryId);
        Map<Long, String> aiStatuses = aiStatusMap(ai);
        Map<Long, String> requestedCorrections = request.states() == null ? Map.of() : request.states().stream()
                .collect(Collectors.toMap(AiReq.UpdateResult.ChecklistFeedback::checklistId,
                        AiReq.UpdateResult.ChecklistFeedback::statusValue, (first, last) -> last));
        FinalResultMerger.MergeResult merged = FinalResultMerger.merge(ai.getAiCategoryId(), categoryId,
                checklists.stream().map(ItemChecklist::getId).collect(Collectors.toSet()),
                aiStatuses, requestedCorrections);
        Map<Long, String> finalStatuses = merged.finalStatuses();
        Map<Long, String> actualCorrections = merged.actualCorrections();
        Long correctedCategoryId = merged.correctedCategoryId();
        UserFeedback feedback = userFeedbackRepository.save(UserFeedback.builder()
                .scanResultId(scanId).correctedCategoryId(correctedCategoryId).comment(request.comment()).build());
        actualCorrections.forEach((id, value) -> userFeedbackDetailRepository.save(UserFeedbackDetail.builder()
                .userFeedbackId(feedback.getId()).checklistId(id).correctedStatusValue(value).build()));

        boolean isPass = checklists.stream().noneMatch(c -> c.isApplicable(finalStatuses.get(c.getId())));
        List<String> steps = checklists.stream().filter(c -> c.isApplicable(finalStatuses.get(c.getId())))
                .map(ItemChecklist::getGuideMessage).toList();
        String source = correctedCategoryId == null ? "AI" : "USER";
        DisposalDecision decision = disposalDecisionRepository.save(DisposalDecision.builder().scanResultId(scanId)
                .userFeedbackId(feedback.getId()).appliedCategoryId(categoryId).categorySource(source)
                .isPass(isPass).guideSnapshot(String.join(" / ", steps)).build());
        checklists.forEach(checklist -> disposalDecisionDetailRepository.save(DisposalDecisionDetail.builder()
                .disposalDecisionId(decision.getId()).checklistId(checklist.getId())
                .statusValue(finalStatuses.get(checklist.getId())).build()));
        return AiRes.ConfirmedResult.builder().scanResultId(scanId).category(category(category, null, source))
                .states(decisionStates(decision, category)).isConfirmed(true).decisionId(decision.getId()).build();
    }

    @Override
    public AiRes.DisposalGuideDetail getDisposalGuide(Long scanId, Long userId) {
        getOwnedScan(scanId, userId);
        DisposalDecision decision = disposalDecisionRepository.findFirstByScanResultIdOrderByCreatedAtDesc(scanId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "확정된 분석 결과를 찾을 수 없습니다."));
        TrashCategory category = trashCategoryRepository.findById(decision.getAppliedCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(String.valueOf(decision.getAppliedCategoryId())));
        DisposalGuide guide = disposalGuideRepository.findByTrashCategoryIdAndIsActiveTrue(category.getId()).orElse(null);
        Map<Long, String> statuses = decisionStatusMap(decision.getId());
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
        scanCommentRepository.save(ScanComment.builder()
                .scanResultId(scanId).userId(userId)
                .comment(comment)
                .build());
    }

    private void requireLegacySingleObject(ScanResult scan) {
        if (scan.getAiRawResponse() == null) return; // Historical single-item scans.
        try {
            AiModelResponse response = objectMapper.readValue(scan.getAiRawResponse(), AiModelResponse.class);
            response.validate();
            if (response.getObjects().size() != 1 || !response.getAdditionalObjects().isEmpty()) {
                throw new ProjectException(GeneralErrorCode.BAD_REQUEST,
                        "다중 객체 또는 추가 후보가 있는 스캔은 objectId 기반 API가 필요합니다.");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalStateException("저장된 AI 응답을 읽을 수 없습니다.", e);
        }
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

    private List<AiRes.Analyze.ChecklistResult> decisionStates(DisposalDecision decision, TrashCategory category) {
        Map<Long, String> statuses = decisionStatusMap(decision.getId());
        List<ItemChecklist> checklists = itemChecklistRepository.findByTrashCategoryIdOrderByDisplayOrder(category.getId());
        return checklists.stream().map(c -> AiRes.Analyze.ChecklistResult.builder().checklistId(c.getId()).checkItemName(c.getCheckItemName())
                .statusValue(statuses.get(c.getId())).confidence(null).build()).toList();
    }

    private Map<Long, String> decisionStatusMap(Long decisionId) {
        return disposalDecisionDetailRepository.findByDisposalDecisionId(decisionId).stream()
                .collect(Collectors.toMap(DisposalDecisionDetail::getChecklistId, DisposalDecisionDetail::getStatusValue));
    }

    private Map<Long, String> aiStatusMap(AiScanResult ai) {
        return aiScanDetailRepository.findByAiScanResultId(ai.getId()).stream()
                .collect(Collectors.toMap(AiScanDetail::getChecklistId, AiScanDetail::getStatusValue));
    }

}
