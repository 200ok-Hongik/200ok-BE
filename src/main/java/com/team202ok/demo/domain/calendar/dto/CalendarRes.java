package com.team202ok.demo.domain.calendar.dto;

import java.time.LocalDateTime;

public record CalendarRes(Long calendarId, Long disposalDecisionId, Long categoryId, String categoryName,
                          LocalDateTime scheduledAt, boolean isCompleted) {
}
