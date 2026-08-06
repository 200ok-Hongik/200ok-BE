package com.team202ok.demo.domain.disposal.repository;

import com.team202ok.demo.domain.disposal.entity.ItemChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemChecklistRepository extends JpaRepository<ItemChecklist, Long> {
    List<ItemChecklist> findByTrashCategoryIdOrderByDisplayOrder(Long trashCategoryId);
}