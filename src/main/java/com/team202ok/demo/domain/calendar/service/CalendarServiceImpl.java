package com.team202ok.demo.domain.calendar.service;

import com.team202ok.demo.domain.calendar.dto.CalendarReq;
import com.team202ok.demo.domain.calendar.dto.CalendarRes;
import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import com.team202ok.demo.domain.calendar.repository.UserCalendarRepository;
import com.team202ok.demo.domain.disposal.entity.DisposalDecision;
import com.team202ok.demo.domain.disposal.repository.DisposalDecisionRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import com.team202ok.demo.global.exception.GeneralErrorCode;
import com.team202ok.demo.global.exception.ProjectException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarServiceImpl implements CalendarService {
    private final UserCalendarRepository userCalendarRepository;
    private final DisposalDecisionRepository disposalDecisionRepository;
    private final TrashCategoryRepository trashCategoryRepository;

    @Override
    public CalendarRes create(Long userId, CalendarReq request) {
        DisposalDecision decision = disposalDecisionRepository.findById(request.disposalDecisionId())
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "분리배출 판단을 찾을 수 없습니다."));
        LocalDateTime scheduledAt = request.scheduledAt() != null ? request.scheduledAt() : LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0);
        return toResponse(userCalendarRepository.save(UserCalendar.builder()
                .userId(userId).disposalDecisionId(decision.getId()).trashCategoryId(decision.getAppliedCategoryId())
                .scheduledAt(scheduledAt).build()));
    }

    @Override
    public List<CalendarRes> getCalendars(Long userId, LocalDate startDate, LocalDate endDate) {
        return userCalendarRepository.findByUserIdAndScheduledAtBetweenOrderByScheduledAtAsc(userId,
                        startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay())
                .stream().map(this::toResponse).toList();
    }

    @Override
    public CalendarRes getCalendar(Long userId, Long calendarId) {
        return toResponse(getOwnedCalendar(userId, calendarId));
    }

    @Override
    public CalendarRes complete(Long userId, Long calendarId) {
        UserCalendar calendar = getOwnedCalendar(userId, calendarId);
        calendar.complete();
        return toResponse(calendar);
    }

    private UserCalendar getOwnedCalendar(Long userId, Long calendarId) {
        UserCalendar calendar = userCalendarRepository.findById(calendarId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "배출 일정을 찾을 수 없습니다."));
        if (!calendar.getUserId().equals(userId)) throw new ProjectException(GeneralErrorCode.FORBIDDEN);
        return calendar;
    }

    private CalendarRes toResponse(UserCalendar calendar) {
        String categoryName = trashCategoryRepository.findById(calendar.getTrashCategoryId())
                .map(category -> category.getName()).orElse("알 수 없는 품목");
        return new CalendarRes(calendar.getId(), calendar.getDisposalDecisionId(), calendar.getTrashCategoryId(),
                categoryName, calendar.getScheduledAt(), calendar.getIsCompleted());
    }
}
