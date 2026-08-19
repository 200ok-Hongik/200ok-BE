package com.team202ok.demo.domain.notification.push;

public record PushMessage(String title, String body, Long calendarId,
                          Long disposalDecisionId, String type) {
}
