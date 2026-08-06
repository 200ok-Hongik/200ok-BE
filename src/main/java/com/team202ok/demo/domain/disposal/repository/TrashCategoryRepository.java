package com.team202ok.demo.domain.disposal.repository;

import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrashCategoryRepository extends JpaRepository<TrashCategory, Long> {
    Optional<TrashCategory> findByCode(String code);
}