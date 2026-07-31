package com.team202ok.demo.recycle.repository;

import com.team202ok.demo.recycle.entity.DisposalGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisposalGuideRepository extends JpaRepository<DisposalGuide, Long> {

    Optional<DisposalGuide> findByTrashCategoryIdAndIsActiveTrue(Long trashCategoryId);
}