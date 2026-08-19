package com.team202ok.demo.domain.analysis.service;

import com.team202ok.demo.global.exception.custom.ProjectException;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinalResultMergerTest {

    @Test
    void appliesOnlyUserCorrectionsOnTopOfImmutableAiResult() {
        Map<Long, String> aiStatuses = Map.of(11L, "YES", 12L, "NO");

        FinalResultMerger.MergeResult result = FinalResultMerger.merge(
                1L, 1L, Set.of(11L, 12L), aiStatuses, Map.of(11L, "NO"));

        assertThat(result.finalStatuses()).containsExactlyInAnyOrderEntriesOf(Map.of(11L, "NO", 12L, "NO"));
        assertThat(result.actualCorrections()).containsExactlyInAnyOrderEntriesOf(Map.of(11L, "NO"));
        assertThat(result.correctedCategoryId()).isNull();
        assertThat(aiStatuses).containsExactlyInAnyOrderEntriesOf(Map.of(11L, "YES", 12L, "NO"));
    }

    @Test
    void ignoresSubmittedValueWhenItMatchesAiOriginal() {
        FinalResultMerger.MergeResult result = FinalResultMerger.merge(
                1L, 1L, Set.of(11L), Map.of(11L, "YES"), Map.of(11L, "YES"));

        assertThat(result.actualCorrections()).isEmpty();
        assertThat(result.finalStatuses()).containsEntry(11L, "YES");
    }

    @Test
    void requiresEveryStateWhenUserCorrectsCategory() {
        assertThatThrownBy(() -> FinalResultMerger.merge(
                1L, 2L, Set.of(21L, 22L), Map.of(11L, "YES"), Map.of(21L, "NO")))
                .isInstanceOf(ProjectException.class)
                .hasMessage("품목을 변경할 때는 새 품목의 모든 체크리스트 상태가 필요합니다.");
    }

    @Test
    void storesNewCategoryAndItsFullStateAsCorrection() {
        FinalResultMerger.MergeResult result = FinalResultMerger.merge(
                1L, 2L, Set.of(21L, 22L), Map.of(11L, "YES"), Map.of(21L, "NO", 22L, "YES"));

        assertThat(result.correctedCategoryId()).isEqualTo(2L);
        assertThat(result.actualCorrections()).containsExactlyInAnyOrderEntriesOf(Map.of(21L, "NO", 22L, "YES"));
        assertThat(result.finalStatuses()).containsExactlyInAnyOrderEntriesOf(Map.of(21L, "NO", 22L, "YES"));
    }
}
