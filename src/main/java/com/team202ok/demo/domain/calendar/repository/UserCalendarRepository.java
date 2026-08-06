package com.team202ok.demo.domain.calendar.repository;

import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCalendarRepository extends JpaRepository<UserCalendar, Long> {
    List<UserCalendar> findByUserIdAndScheduledAtBetweenOrderByScheduledAtAsc(
            Long userId, LocalDateTime start, LocalDateTime end);
}
