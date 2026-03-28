package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dat.erp.constants.DayType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.CompanyCalendarDateRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.mapper.interfaces.CalendarDateMapper;
import com.dat.erp.repositories.customrepositories.CalendarDateRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.base.AbstractAuditableService;

@Service
public class CalendarDateServiceImpl extends AbstractAuditableService implements CalendarDateService {

    private final CalendarDateRepository calendarDateRepository;
    private final CalendarDateMapper calendarDateMapper;

    public CalendarDateServiceImpl(
            CalendarDateRepository calendarDateRepository,
            CalendarDateMapper calendarDateMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.calendarDateRepository = calendarDateRepository;
        this.calendarDateMapper = calendarDateMapper;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public List<CalendarDate> createCalendarDates(List<CompanyCalendarDateRequest> requests, CompanyCalendar calendar) {
        validateCalendar(calendar);
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATES_INVALID);
        }

        List<CalendarDate> calendarDates = calendarDateMapper.toEntities(requests);
        Set<LocalDate> uniqueDates = new HashSet<>();

        for (CalendarDate calendarDate : calendarDates) {
            if (calendarDate == null) {
                throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATES_INVALID);
            }

            LocalDate calDate = calendarDate.getCalDate();
            if (calDate == null) {
                throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_CAL_DATE_INVALID);
            }

            if (calendarDate.getDayType() == null) {
                throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DAY_TYPE_INVALID);
            }

            if (calendarDate.getNote() == null || calendarDate.getNote().isBlank()) {
                throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATE_NOTE_INVALID);
            }

            if (calDate.isBefore(calendar.getEffectiveFrom()) || calDate.isAfter(calendar.getEffectiveTo())) {
                throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATE_OUT_OF_RANGE);
            }

            if (!uniqueDates.add(calDate)) {
                throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_DATE_DUPLICATE);
            }

            calendarDate.setCalendar(calendar);
            generateCodeIfMissing(calendarDate, CodePrefixes.CALENDAR_DATE);
            applyInsertAudit(calendarDate);
        }

        return calendarDateRepository.saveAll(calendarDates);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyCalendarDateResponse> getCompanyCalendarDates(CompanyCalendar calendar, Integer year) {
        validateCalendar(calendar);
        if (year == null || year < 1) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_YEAR_INVALID);
        }

        LocalDate fromDate = LocalDate.of(year, 1, 1);
        LocalDate toDate = LocalDate.of(year, 12, 31);

        return calendarDateRepository.findByCalendarCodeAndCompanyCodeAndDateRange(
                calendar.getCode(),
                calendar.getCompanyCode(),
                fromDate,
                toDate).stream()
                .map(calendarDateMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<DayType, Integer> getCalendarDateTotalsByCompanyCodeAndMonth(String companyCode, YearMonth month) {
        if (companyCode == null || companyCode.isBlank()) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_COMPANY_CODE_INVALID);
        }
        if (month == null) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_MONTH_INVALID);
        }

        LocalDate fromDate = month.atDay(1);
        LocalDate toDate = month.atEndOfMonth();

        Map<DayType, Integer> totalsByDayType = new EnumMap<>(DayType.class);
        calendarDateRepository.countByCompanyCodeAndDateRangeGroupByDayType(companyCode.trim(), fromDate, toDate)
                .forEach(item -> totalsByDayType.put(item.getDayType(), Math.toIntExact(item.getTotalDates())));
        return totalsByDayType;
    }

    private void validateCalendar(CompanyCalendar calendar) {
        if (calendar == null || calendar.getCode() == null || calendar.getCode().isBlank()) {
            throw new BadRequestException("calendar is invalid");
        }
        if (calendar.getEffectiveFrom() == null || calendar.getEffectiveTo() == null) {
            throw new BadRequestException(Messages.ERROR_COMPANY_CALENDAR_EFFECTIVE_DATES_INVALID);
        }
    }
}
