package com.team202ok.demo.domain.calendar.dto;

import java.time.LocalDateTime;

public record CalendarReq(Long disposalDecisionId, LocalDateTime scheduledAt) {
}
