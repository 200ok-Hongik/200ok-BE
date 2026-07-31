package com.team202ok.demo.recycle.repository;

import com.team202ok.demo.recycle.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RagRecycleRepository extends JpaRepository<Rule, Long> {

    Optional<Rule> findByItemName(String itemName);

    Optional<Rule> findBySourceUrl(String sourceUrl);

    Optional<Rule> findFirstByItemNameContaining(String itemName);
}
