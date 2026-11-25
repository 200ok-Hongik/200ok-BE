package com.team202ok.demo.domain.calendar.service;

import com.team202ok.demo.domain.calendar.dto.CalendarReq;
import com.team202ok.demo.domain.calendar.dto.CalendarRes;
import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import com.team202ok.demo.domain.calendar.repository.UserCalendarRepository;
import com.team202ok.demo.domain.ai.entity.ScanResult;
import com.team202ok.demo.domain.ai.repository.ScanResultRepository;
import com.team202ok.demo.domain.disposal.entity.DisposalDecision;
import com.team202ok.demo.domain.disposal.entity.TrashCategory;
import com.team202ok.demo.domain.disposal.repository.DisposalDecisionRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import com.team202ok.demo.domain.region.service.RegionDischargeService;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.domain.user.repository.UserRepository;
import com.team202ok.demo.global.exception.code.GeneralErrorCode;
import com.team202ok.demo.global.exception.custom.ProjectException;
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
    private final UserRepository userRepository;
    private final RegionDischargeService regionDischargeService;
    private final ScanResultRepository scanResultRepository;

    @Override
    public CalendarRes create(Long userId, CalendarReq request) {
        DisposalDecision decision = disposalDecisionRepository.findById(request.disposalDecisionId())
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "분리배출 판단을 찾을 수 없습니다."));
        ScanResult scan = scanResultRepository.findById(decision.getScanResultId())
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "스캔 결과를 찾을 수 없습니다."));
        if (!scan.getUserId().equals(userId)) {
            throw new ProjectException(GeneralErrorCode.FORBIDDEN);
        }
        LocalDateTime scheduledAt = request.scheduledAt() != null
                ? request.scheduledAt() : nextSchedule(userId, decision.getAppliedCategoryId());
        return toResponse(userCalendarRepository.save(UserCalendar.builder()
                .userId(userId).disposalDecisionId(decision.getId()).trashCategoryId(decision.getAppliedCategoryId())
                .scheduledAt(scheduledAt).build()));
    }

    @Override
    public List<CalendarRes> getCalendars(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "시작일은 종료일보다 늦을 수 없습니다.");
        }
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

    private LocalDateTime nextSchedule(Long userId, Long categoryId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        if (user.getRegionCode() == null) throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "기본 배출 지역을 먼저 설정해 주세요.");
        String categoryCode = trashCategoryRepository.findById(categoryId).map(TrashCategory::getCode).orElse(null);
        RegionDischargeService.Schedule schedule = regionDischargeService.findSchedule(user.getRegionCode(), categoryCode)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "지역 배출 일정을 찾을 수 없습니다."));
        return schedule.nextOccurrence(regionDischargeService.now());
    }
}
