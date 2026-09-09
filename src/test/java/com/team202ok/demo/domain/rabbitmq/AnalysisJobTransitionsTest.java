package com.team202ok.demo.domain.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"})
@Import(AnalysisJobTransitions.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AnalysisJobTransitionsTest {
    @Autowired AnalysisJobRepository jobs;
    @Autowired AnalysisJobTransitions transitions;
    @Autowired JdbcTemplate jdbc;

    private String submit() {
        return jobs.save(new AnalysisJob(UUID.randomUUID().toString(), 1L,
                "test.png", "image/png", new byte[]{1})).getId();
    }

    @Test void commitsProcessingBeforeAnalysisAndSuppressesDuplicateDispatchAndClaim() {
        String id = submit();
        assertThat(transitions.reserveDispatch(id)).isTrue();
        assertThat(transitions.reserveDispatch(id)).isFalse();
        var claimed = transitions.claim(id);
        assertThat(jobs.findByIdAndUserId(id, 1L).orElseThrow().getStatus()).isEqualTo("PROCESSING");
        assertThat(transitions.claim(id)).isNull();
        assertThat(transitions.reserveDispatch(id)).isFalse();
        transitions.finish(id, claimed.getUpdatedAt(), "{}", null);
        var completed = jobs.findById(id).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo("COMPLETED");
        assertThat(completed.getImage()).isNull();
        assertThat(transitions.reserveDispatch(id)).isFalse();
        assertThat(transitions.claim(id)).isNull();
    }

    @Test void recoversStalledWorkerWithoutAcceptingItsLateResult() {
        String id = submit();
        var oldClaim = transitions.claim(id);
        expire(id);
        assertThat(transitions.reserveDispatch(id)).isTrue();
        var newClaim = transitions.claim(id);
        transitions.finish(id, oldClaim.getUpdatedAt(), "old", null);
        assertThat(jobs.findById(id).orElseThrow().getStatus()).isEqualTo("PROCESSING");
        transitions.finish(id, newClaim.getUpdatedAt(), null, "failed");
        assertThat(jobs.findById(id).orElseThrow().getStatus()).isEqualTo("FAILED");
        assertThat(jobs.findById(id).orElseThrow().getImage()).isNull();
    }

    @Test void retriesLostPublishOnlyAfterRecoveryInterval() {
        String id = submit();
        assertThat(transitions.reserveDispatch(id)).isTrue();
        var cutoff = Instant.now().minusSeconds(AnalysisJobTransitions.RECOVERY_SECONDS);
        assertThat(jobs.findPendingIds(cutoff, org.springframework.data.domain.PageRequest.of(0, 100))).doesNotContain(id);
        expire(id);
        assertThat(jobs.findPendingIds(cutoff, org.springframework.data.domain.PageRequest.of(0, 100))).contains(id);
        assertThat(transitions.reserveDispatch(id)).isTrue();
    }

    private void expire(String id) {
        jdbc.update("update ai_analysis_jobs set updated_at = ? where id = ?",
                Timestamp.from(Instant.now().minusSeconds(AnalysisJobTransitions.RECOVERY_SECONDS + 1)), id);
    }
}
