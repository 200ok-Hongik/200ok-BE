package com.team202ok.demo.domain.ai.service;

import com.team202ok.demo.global.exception.code.GeneralErrorCode;
import com.team202ok.demo.global.exception.custom.ProjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class FinalResultMerger {

    private FinalResultMerger() {
    }

    static MergeResult merge(Long aiCategoryId, Long selectedCategoryId, Set<Long> checklistIds,
                             Map<Long, String> aiStatuses, Map<Long, String> requestedCorrections) {
        if (!checklistIds.containsAll(requestedCorrections.keySet())) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "선택한 품목의 체크리스트가 아닙니다.");
        }

        boolean categoryChanged = !selectedCategoryId.equals(aiCategoryId);
        if (categoryChanged && !requestedCorrections.keySet().containsAll(checklistIds)) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST,
                    "품목을 변경할 때는 새 품목의 모든 체크리스트 상태가 필요합니다.");
        }

        Map<Long, String> finalStatuses = new HashMap<>();
        if (!categoryChanged) {
            finalStatuses.putAll(aiStatuses);
        }
        finalStatuses.putAll(requestedCorrections);

        if (checklistIds.stream().anyMatch(id -> finalStatuses.get(id) == null)) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST,
                    "확정에 필요한 모든 체크리스트 상태를 입력해 주세요.");
        }

        Map<Long, String> actualCorrections = new HashMap<>();
        requestedCorrections.forEach((id, value) -> {
            if (!Objects.equals(aiStatuses.get(id), value)) {
                actualCorrections.put(id, value);
            }
        });
        return new MergeResult(Map.copyOf(finalStatuses), Map.copyOf(actualCorrections),
                categoryChanged ? selectedCategoryId : null);
    }

    record MergeResult(Map<Long, String> finalStatuses, Map<Long, String> actualCorrections,
                       Long correctedCategoryId) {
    }
}
