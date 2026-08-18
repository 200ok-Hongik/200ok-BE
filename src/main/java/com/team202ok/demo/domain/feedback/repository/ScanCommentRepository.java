package com.team202ok.demo.domain.feedback.repository;

import com.team202ok.demo.domain.feedback.entity.ScanComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanCommentRepository extends JpaRepository<ScanComment, Long> {
}
