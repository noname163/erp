package com.dat.erp.services.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

@Service
public class CalendarDateServiceImpl implements CalendarDateService {

    private final CalendarDateRepository calendarDateRepository;
    private final CalendarDateMapper calendarDateMapper;

    public CalendarDateServiceImpl(
            CalendarDateRepository calendarDateRepository,
            CalendarDateMapper calendarDateMapper,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.calendarDateRepository = calendarDateRepository;
        this.calendarDateMapper = calendarDateMapper;
    }

    @Override
    @Transactional
    public List<CalendarDate> createCalendarDates(List<CompanyCalendarDateRequest> requests, CompanyCalendar calendar) {
        validateCalendar(calendar);
        List<CalendarDate> calendarDates = mapAndValidateCalendarDates(requests, calendar);
        for (CalendarDate calendarDate : calendarDates) {
            calendarDate.setCalendar(calendar);
        }

        return calendarDateRepository.saveAll(calendarDates);
    }

    @Override
    @Transactional
    public List<CalendarDate> replaceCalendarDates(List<CompanyCalendarDateRequest> requests, CompanyCalendar calendar) {
        validateCalendar(calendar);
        List<CalendarDate> requestedCalendarDates = mapAndValidateCalendarDates(requests, calendar);
        Map<LocalDate, CalendarDate> existingByDate = new LinkedHashMap<>();
        calendarDateRepository.findByCalendarCodeAndCompanyCode(calendar.getCode(), calendar.getCompanyCode())
                .forEach(calendarDate -> existingByDate.put(calendarDate.getCalDate(), calendarDate));

        List<CalendarDate> calendarDatesToCreate = new ArrayList<>();
        List<CalendarDate> orderedCalendarDates = new ArrayList<>();
        for (CalendarDate requestedCalendarDate : requestedCalendarDates) {
            CalendarDate existingCalendarDate = existingByDate.remove(requestedCalendarDate.getCalDate());
            if (existingCalendarDate != null) {
                existingCalendarDate.setDayType(requestedCalendarDate.getDayType());
                existingCalendarDate.setNote(requestedCalendarDate.getNote());
                orderedCalendarDates.add(existingCalendarDate);
                continue;
            }

            requestedCalendarDate.setCalendar(calendar);
            calendarDatesToCreate.add(requestedCalendarDate);
            orderedCalendarDates.add(requestedCalendarDate);
        }

        if (!existingByDate.isEmpty()) {
            calendarDateRepository.deleteAll(existingByDate.values());
        }

        if (!calendarDatesToCreate.isEmpty()) {
            calendarDateRepository.saveAll(calendarDatesToCreate);
        }

        return orderedCalendarDates;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyCalendarDateResponse> getCompanyCalendarDates(CompanyCalendar calendar) {
        validateCalendar(calendar);
        return calendarDateRepository.findByCalendarCodeAndCompanyCode(
                calendar.getCode(),
                calendar.getCompanyCode()).stream()
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

    private List<CalendarDate> mapAndValidateCalendarDates(List<CompanyCalendarDateRequest> requests, CompanyCalendar calendar) {
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
        }
        return calendarDates;
    }
}
