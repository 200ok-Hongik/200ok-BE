package com.team202ok.demo.recycle.repository;

import com.team202ok.demo.recycle.entity.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {

}