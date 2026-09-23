package com.dat.erp.services.impl;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.CompanyCalendarRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.dto.response.CompanyCalendarListResponse;
import com.dat.erp.dto.response.CompanyCalendarResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.CompanyCalendarMapper;
import com.dat.erp.repositories.customrepositories.CompanyCalendarRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.CompanyCalendarService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.CustomStringUtils;
import com.dat.erp.utils.PageableUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CompanyCalendarServiceImpl  implements CompanyCalendarService {

    private final CompanyCalendarRepository companyCalendarRepository;
    private final CalendarDateService calendarDateService;
    private final CompanyCalendarMapper companyCalendarMapper;
    private final SecurityContextService securityContextService;
    private final CodeGenerator codeGenerator;

    @Override
    @Transactional
    public CompanyCalendarResponse createCompanyCalendar(CompanyCalendarRequest request) {
        if (request == null) {
            throw new BadRequestException("request is invalid");
        }

        if (request.getDates() == null || request.getDates().isEmpty()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATES_INVALID);
        }

        LocalDate effectiveFrom = request.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();
        validateEffectiveDates(effectiveFrom, effectiveTo);

        String companyCode = securityContextService.getCurrentCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        CompanyCalendar calendar = companyCalendarMapper.toEntity(request);
        validateCalendar(calendar);
        calendar.initializeCode(codeGenerator.nextCode(CodePrefixes.COMPANY_CALENDAR));

        CompanyCalendar savedCalendar = companyCalendarRepository.save(calendar);
        List<CalendarDate> savedDates = calendarDateService.createCalendarDates(request.getDates(), savedCalendar);
        savedCalendar.setDates(savedDates.stream()
                .sorted(Comparator.comparing(CalendarDate::getCalDate))
                .toList());
        return companyCalendarMapper.toResponse(savedCalendar);
    }

    @Override
    @Transactional
    public CompanyCalendarResponse updateCompanyCalendar(String code, CompanyCalendarRequest request) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_CODE_INVALID);
        }
        if (request == null) {
            throw new BadRequestException("request is invalid");
        }
        if (request.getDates() == null || request.getDates().isEmpty()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATES_INVALID);
        }

        validateEffectiveDates(request.getEffectiveFrom(), request.getEffectiveTo());

        CompanyCalendar calendar = companyCalendarRepository.findByCodeAndCompanyCodeAndIsDeletedFalse(
                code.trim(),
                securityContextService.getCurrentCompanyCode())
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_COMPANY_CALENDAR_NOT_FOUND));

        companyCalendarMapper.updateEntity(request, calendar);
        validateCalendar(calendar);

        CompanyCalendar savedCalendar = companyCalendarRepository.save(calendar);
        List<CalendarDate> savedDates = calendarDateService.replaceCalendarDates(request.getDates(), savedCalendar);
        savedCalendar.setDates(savedDates.stream()
                .sorted(Comparator.comparing(CalendarDate::getCalDate))
                .toList());
        return companyCalendarMapper.toResponse(savedCalendar);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CompanyCalendarListResponse> getCompanyCalendars(
            String name,
            String region,
            String timeZone,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir) {
        String companyCode = securityContextService.getCurrentCompanyCode();
        String normalizedTimeZone = normalizeOptionalTimeZone(timeZone);
        Pageable pageable = PageableUtils.create(page, size, sortBy, sortDir);
        Page<CompanyCalendar> calendars = companyCalendarRepository.searchByConditions(
                companyCode,
                CustomStringUtils.trimToNull(name),
                CustomStringUtils.trimToNull(region),
                normalizedTimeZone,
                pageable);
        return PageableUtils.mapPage(calendars, companyCalendarMapper::toListResponse, Messages.SUCCESS);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyCalendarDateResponse> getCompanyCalendarDates(String code) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_CODE_INVALID);
        }

        CompanyCalendar calendar = companyCalendarRepository.findByCodeAndCompanyCodeAndIsDeletedFalse(
                code.trim(),
                securityContextService.getCurrentCompanyCode())
                .orElseThrow(() -> new ResourceNotFoundException(Messages.ERROR_COMPANY_CALENDAR_NOT_FOUND));

        return calendarDateService.getCompanyCalendarDates(calendar);
    }

    private void validateEffectiveDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom == null || effectiveTo == null) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_EFFECTIVE_DATES_INVALID);
        }
    }

    private void validateCalendar(CompanyCalendar calendar) {
        if (calendar == null) {
            throw new BadRequestException("request is invalid");
        }
        if (calendar.getName() == null || calendar.getName().isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_NAME_INVALID);
        }
        if (calendar.getRegion() == null || calendar.getRegion().isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_REGION_INVALID);
        }
        if (calendar.getNote() == null || calendar.getNote().isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_NOTE_INVALID);
        }
        if (calendar.getTimeZone() == null || calendar.getTimeZone().isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_TIME_ZONE_INVALID);
        }
        try {
            calendar.setTimeZone(ZoneId.of(calendar.getTimeZone().trim()).getId());
        } catch (DateTimeException ex) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_TIME_ZONE_INVALID);
        }
    }

    private String normalizeOptionalTimeZone(String timeZone) {
        if (timeZone == null || timeZone.isBlank()) {
            return null;
        }
        try {
            return ZoneId.of(timeZone.trim()).getId();
        } catch (DateTimeException ex) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_TIME_ZONE_INVALID);
        }
    }
}
