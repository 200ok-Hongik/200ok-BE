package com.team202ok.demo.domain.feedback.repository;

import com.team202ok.demo.domain.feedback.entity.UserFeedbackDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFeedbackDetailRepository extends JpaRepository<UserFeedbackDetail, Long> {
}
