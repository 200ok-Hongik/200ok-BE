package com.team202ok.demo.domain.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

/** Short transactions: no broker or AI network calls while holding a job lock. */
@Service
@RequiredArgsConstructor
public class AnalysisJobTransitions {
    // Longer than the 120-second AI timeout. Also recovers jobs after process/broker loss.
    public static final long RECOVERY_SECONDS = 300;
    private final AnalysisJobRepository jobs;

    @Transactional
    public boolean reserveDispatch(String id) {
        var job = jobs.findLocked(id).orElse(null);
        if (job == null || terminal(job)) return false;
        boolean stale = job.getUpdatedAt().isBefore(Instant.now().minusSeconds(RECOVERY_SECONDS));
        boolean fresh = "QUEUED".equals(job.getStatus()) && job.getUpdatedAt().equals(job.getCreatedAt());
        if (!fresh && !stale) return false;
        job.queued();
        return true;
    }

    @Transactional
    public AnalysisJob claim(String id) {
        var job = jobs.findLocked(id).orElse(null);
        if (job == null || !"QUEUED".equals(job.getStatus())) return null;
        job.processing();
        return job;
    }

    @Transactional
    public void finish(String id, Instant claimTime, String result, String error) {
        var job = jobs.findLocked(id).orElseThrow();
        // An expired worker must not overwrite a newer attempt.
        if (!"PROCESSING".equals(job.getStatus()) || !job.getUpdatedAt().equals(claimTime)) return;
        if (error == null) job.complete(result); else job.fail(error);
    }

    private boolean terminal(AnalysisJob job) {
        return "COMPLETED".equals(job.getStatus()) || "FAILED".equals(job.getStatus());
    }
}
