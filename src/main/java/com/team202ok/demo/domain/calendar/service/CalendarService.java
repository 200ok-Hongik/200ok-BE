package com.team202ok.demo.domain.calendar.service;

import com.team202ok.demo.domain.calendar.dto.CalendarReq;
import com.team202ok.demo.domain.calendar.dto.CalendarRes;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    CalendarRes create(Long userId, CalendarReq request);
    List<CalendarRes> getCalendars(Long userId, LocalDate startDate, LocalDate endDate);
    CalendarRes getCalendar(Long userId, Long calendarId);
    CalendarRes complete(Long userId, Long calendarId);
}
