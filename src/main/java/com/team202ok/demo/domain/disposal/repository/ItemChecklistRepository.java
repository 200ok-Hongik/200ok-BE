package com.team202ok.demo.domain.disposal.repository;

import com.team202ok.demo.domain.disposal.entity.ItemChecklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemChecklistRepository extends JpaRepository<ItemChecklist, Long> {
    List<ItemChecklist> findByTrashCategoryIdOrderByDisplayOrder(Long trashCategoryId);

    Optional<ItemChecklist> findByTrashCategoryIdAndCheckItemName(Long trashCategoryId, String checkItemName);
}
