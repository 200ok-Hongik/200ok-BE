package com.team202ok.demo.domain.feedback.repository;

import com.team202ok.demo.domain.feedback.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    Optional<UserFeedback> findFirstByScanResultIdOrderByCreatedAtDesc(Long scanResultId);
}
