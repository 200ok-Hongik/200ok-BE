package com.team202ok.demo.domain.calendar.controller;

import com.team202ok.demo.domain.calendar.dto.CalendarReq;
import com.team202ok.demo.domain.calendar.dto.CalendarRes;
import com.team202ok.demo.domain.calendar.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @PostMapping
    public CalendarRes create(@RequestParam Long userId, @RequestBody CalendarReq request) { return calendarService.create(userId, request); }

    @GetMapping
    public List<CalendarRes> getCalendars(@RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return calendarService.getCalendars(userId, startDate, endDate);
    }

    @GetMapping("/{calendarId}")
    public CalendarRes getCalendar(@RequestParam Long userId, @PathVariable Long calendarId) { return calendarService.getCalendar(userId, calendarId); }

    @PatchMapping("/{calendarId}/complete")
    public CalendarRes complete(@RequestParam Long userId, @PathVariable Long calendarId) { return calendarService.complete(userId, calendarId); }
}
