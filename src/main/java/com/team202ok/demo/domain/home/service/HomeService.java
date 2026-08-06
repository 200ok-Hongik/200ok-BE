package com.team202ok.demo.domain.home.service;

import com.team202ok.demo.domain.home.dto.HomeRes;

import java.util.List;

public interface HomeService {
    HomeRes.Summary getSummary(Long userId);
    List<HomeRes.TodaySchedule> getTodaySchedules(Long userId);
    List<HomeRes.NotificationItem> getRecentNotifications(Long userId);
}
