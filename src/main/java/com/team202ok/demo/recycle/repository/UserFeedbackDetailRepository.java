package com.team202ok.demo.recycle.repository;

import com.team202ok.demo.recycle.entity.UserFeedbackDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFeedbackDetailRepository extends JpaRepository<UserFeedbackDetail, Long> {

    List<UserFeedbackDetail> findByUserFeedbackId(Long userFeedbackId);
}