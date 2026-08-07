package com.team202ok.demo.domain.calendar.service;

import com.team202ok.demo.domain.calendar.dto.CalendarReq;
import com.team202ok.demo.domain.calendar.dto.CalendarRes;
import com.team202ok.demo.domain.calendar.entity.UserCalendar;
import com.team202ok.demo.domain.calendar.repository.UserCalendarRepository;
import com.team202ok.demo.domain.disposal.entity.DisposalDecision;
import com.team202ok.demo.domain.disposal.repository.DisposalDecisionRepository;
import com.team202ok.demo.domain.disposal.repository.TrashCategoryRepository;
import com.team202ok.demo.domain.region.entity.RegionSchedule;
import com.team202ok.demo.domain.region.repository.RegionScheduleRepository;
import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.domain.user.repository.UserRepository;
import com.team202ok.demo.global.exception.GeneralErrorCode;
import com.team202ok.demo.global.exception.ProjectException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarServiceImpl implements CalendarService {
    private final UserCalendarRepository userCalendarRepository;
    private final DisposalDecisionRepository disposalDecisionRepository;
    private final TrashCategoryRepository trashCategoryRepository;
    private final UserRepository userRepository;
    private final RegionScheduleRepository regionScheduleRepository;

    @Override
    public CalendarRes create(Long userId, CalendarReq request) {
        DisposalDecision decision = disposalDecisionRepository.findById(request.disposalDecisionId())
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "분리배출 판단을 찾을 수 없습니다."));
        LocalDateTime scheduledAt = request.scheduledAt() != null ? request.scheduledAt() : nextSchedule(userId);
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

    private LocalDateTime nextSchedule(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        if (user.getRegionCode() == null) throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "기본 배출 지역을 먼저 설정해 주세요.");
        RegionSchedule schedule = regionScheduleRepository.findByRegionCode(user.getRegionCode())
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND, "지역 배출 일정을 찾을 수 없습니다."));
        Set<DayOfWeek> days = Arrays.stream(schedule.getDischargeDays().split(","))
                .map(String::trim).map(this::toDayOfWeek).collect(Collectors.toSet());
        LocalTime startTime = LocalTime.parse(schedule.getDischargeTime().split("~")[0]);
        LocalDateTime now = LocalDateTime.now();
        for (int offset = 0; offset <= 7; offset++) {
            LocalDateTime candidate = now.toLocalDate().plusDays(offset).atTime(startTime);
            if (days.contains(candidate.getDayOfWeek()) && candidate.isAfter(now)) return candidate;
        }
        throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "지역 배출 요일 형식이 올바르지 않습니다.");
    }

    private DayOfWeek toDayOfWeek(String day) {
        return switch (day) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            case "토" -> DayOfWeek.SATURDAY;
            case "일" -> DayOfWeek.SUNDAY;
            default -> throw new ProjectException(GeneralErrorCode.BAD_REQUEST, "알 수 없는 배출 요일입니다: " + day);
        };
    }
}
