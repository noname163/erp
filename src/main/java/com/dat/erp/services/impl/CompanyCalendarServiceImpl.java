package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.payroll.CalendarDateRequest;
import com.dat.erp.dto.request.payroll.CompanyCalendarRequest;
import com.dat.erp.dto.response.payroll.CompanyCalendarResponse;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.CalendarDateRepository;
import com.dat.erp.repositories.customrepositories.CompanyCalendarRepository;
import com.dat.erp.services.CompanyCalendarService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class CompanyCalendarServiceImpl extends AbstractAuditableService implements CompanyCalendarService {

    private final CompanyCalendarRepository companyCalendarRepository;
    private final CalendarDateRepository calendarDateRepository;

    public CompanyCalendarServiceImpl(
            CompanyCalendarRepository companyCalendarRepository,
            CalendarDateRepository calendarDateRepository) {
        this.companyCalendarRepository = companyCalendarRepository;
        this.calendarDateRepository = calendarDateRepository;
    }

    @Override
    @Transactional
    public CompanyCalendarResponse create(CompanyCalendarRequest request) {
        validateRequest(request);
        CompanyCalendar calendar = new CompanyCalendar();
        mapCalendar(calendar, request);
        generateCodeIfMissing(calendar, CodePrefixes.COMPANY_CALENDAR);
        applyInsertAudit(calendar);
        CompanyCalendar savedCalendar = companyCalendarRepository.save(calendar);
        List<CalendarDate> dates = saveDates(savedCalendar, request.dates());
        return toResponse(savedCalendar, dates);
    }

    @Override
    @Transactional
    public CompanyCalendarResponse update(String code, CompanyCalendarRequest request) {
        validateRequest(request);
        CompanyCalendar calendar = findOwnedCalendar(code);
        mapCalendar(calendar, request);
        applyUpdateAudit(calendar);
        CompanyCalendar savedCalendar = companyCalendarRepository.save(calendar);
        softDeleteDates(savedCalendar.getCode());
        List<CalendarDate> dates = saveDates(savedCalendar, request.dates());
        return toResponse(savedCalendar, dates);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyCalendarResponse get(String code) {
        CompanyCalendar calendar = findOwnedCalendar(code);
        return toResponse(calendar, findDates(calendar.getCode()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyCalendarResponse> list() {
        String companyCode = resolveCurrentUserCompanyCode();
        return companyCalendarRepository.findByCompanyCodeAndIsDeletedFalseOrderByEffectiveFromDesc(companyCode).stream()
                .map(calendar -> toResponse(calendar, findDates(calendar.getCode())))
                .toList();
    }

    @Override
    @Transactional
    public void delete(String code) {
        CompanyCalendar calendar = findOwnedCalendar(code);
        calendar.setIsDeleted(true);
        applyUpdateAudit(calendar);
        companyCalendarRepository.save(calendar);
        softDeleteDates(calendar.getCode());
    }

    private void validateRequest(CompanyCalendarRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_NAME_INVALID);
        }
        if (request.effectiveFrom() == null || request.effectiveTo() == null || request.effectiveFrom().isAfter(request.effectiveTo())) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATES_INVALID);
        }
        Set<LocalDate> seenDates = new HashSet<>();
        for (CalendarDateRequest date : request.dates() == null ? List.<CalendarDateRequest>of() : request.dates()) {
            if (date == null || date.calDate() == null || date.dayType() == null || !seenDates.add(date.calDate())) {
                throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATES_INVALID);
            }
        }
    }

    private CompanyCalendar findOwnedCalendar(String code) {
        CompanyCalendar calendar = companyCalendarRepository.findByCodeAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_COMPANY_CALENDAR_NOT_FOUND));
        if (!Objects.equals(calendar.getCompanyCode(), resolveCurrentUserCompanyCode())) {
            throw new ResourceNotFoundException(Messages.ERROR_COMPANY_CALENDAR_NOT_FOUND);
        }
        return calendar;
    }

    private void mapCalendar(CompanyCalendar calendar, CompanyCalendarRequest request) {
        calendar.setName(request.name().trim());
        calendar.setEffectiveFrom(request.effectiveFrom());
        calendar.setEffectiveTo(request.effectiveTo());
    }

    private List<CalendarDate> saveDates(CompanyCalendar calendar, List<CalendarDateRequest> requests) {
        List<CalendarDate> dates = new ArrayList<>();
        if (requests == null || requests.isEmpty()) {
            return dates;
        }
        for (CalendarDateRequest request : requests) {
            CalendarDate date = new CalendarDate();
            date.setCalendar(calendar);
            date.setCalDate(request.calDate());
            date.setDayType(request.dayType());
            date.setIsPaidHoliday(request.isPaidHoliday());
            date.setHolidayName(request.holidayName());
            generateCodeIfMissing(date, CodePrefixes.CALENDAR_DATE);
            applyInsertAudit(date);
            dates.add(date);
        }
        return calendarDateRepository.saveAll(dates);
    }

    private void softDeleteDates(String calendarCode) {
        List<CalendarDate> dates = findDates(calendarCode);
        if (dates.isEmpty()) {
            return;
        }
        for (CalendarDate date : dates) {
            date.setIsDeleted(true);
            applyUpdateAudit(date);
        }
        calendarDateRepository.saveAll(dates);
    }

    private List<CalendarDate> findDates(String calendarCode) {
        return calendarDateRepository.findByCalendar_CodeAndIsDeletedFalseOrderByCalDateAsc(calendarCode);
    }

    private CompanyCalendarResponse toResponse(CompanyCalendar calendar, List<CalendarDate> dates) {
        return new CompanyCalendarResponse(
                calendar.getCode(),
                calendar.getName(),
                calendar.getEffectiveFrom(),
                calendar.getEffectiveTo(),
                dates.stream()
                        .map(date -> new CalendarDateRequest(date.getCalDate(), date.getDayType(), date.getIsPaidHoliday(),
                                date.getHolidayName()))
                        .toList());
    }
}
