package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.CompanyCalendarDateRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.mapper.interfaces.CalendarDateMapper;
import com.dat.erp.repositories.customrepositories.CalendarDateRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class CalendarDateServiceImplTest {

    @Mock
    private CalendarDateRepository calendarDateRepository;

    @Mock
    private CalendarDateMapper calendarDateMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @Captor
    private ArgumentCaptor<List<CalendarDate>> captor;

    @InjectMocks
    private CalendarDateServiceImpl calendarDateService;

    private CompanyCalendar calendar;
    private List<CompanyCalendarDateRequest> requests;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        calendar = CompanyCalendar.builder()
                .name("2026 Standard Calendar")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 1, 31))
                .build();
        calendar.setCode("CCA-000001");
        calendar.setCompanyCode("CMP-001");

        CompanyCalendarDateRequest firstDate = new CompanyCalendarDateRequest();
        firstDate.setCalDate(LocalDate.of(2026, 1, 1));
        firstDate.setDayType(DayType.HOLIDAY_WORK);
        firstDate.setNote("New Year holiday");

        CompanyCalendarDateRequest secondDate = new CompanyCalendarDateRequest();
        secondDate.setCalDate(LocalDate.of(2026, 1, 4));
        secondDate.setDayType(DayType.WEEKEND_WORK);
        secondDate.setNote("First weekend");
        requests = List.of(firstDate, secondDate);

        Account account = new Account();
        account.setCode("ACC-001");
        account.setCompanyCode("CMP-001");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
    }

    @Test
    void createCalendarDates_success() {
        List<CalendarDate> mappedDates = List.of(
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 1)).dayType(DayType.HOLIDAY_WORK).note("New Year holiday").build(),
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 4)).dayType(DayType.WEEKEND_WORK).note("First weekend").build());
        when(calendarDateMapper.toEntities(requests)).thenReturn(mappedDates);
        when(codeGenerator.nextCode("CAD-")).thenReturn("CAD-000001", "CAD-000002");
        when(calendarDateRepository.saveAll(ArgumentMatchers.<List<CalendarDate>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<CalendarDate> result = calendarDateService.createCalendarDates(requests, calendar);

        assertEquals(2, result.size());
        assertEquals("CAD-000001", result.get(0).getCode());
        assertEquals("CAD-000002", result.get(1).getCode());

        verify(calendarDateRepository).saveAll(captor.capture());
        assertEquals(calendar, captor.getValue().get(0).getCalendar());
        assertEquals("CMP-001", captor.getValue().get(0).getCompanyCode());
    }

    @Test
    void createCalendarDates_badRequestWhenDuplicateDateExists() {
        List<CalendarDate> mappedDates = List.of(
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 1)).dayType(DayType.HOLIDAY_WORK).note("New Year holiday").build(),
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 1)).dayType(DayType.WEEKEND_WORK).note("Duplicate weekend").build());
        when(calendarDateMapper.toEntities(requests)).thenReturn(mappedDates);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> calendarDateService.createCalendarDates(requests, calendar));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_DATE_DUPLICATE, ex.getMessage());
        verify(calendarDateRepository, never()).saveAll(any());
    }

    @Test
    void createCalendarDates_badRequestWhenDateOutOfRange() {
        CompanyCalendarDateRequest singleRequest = requests.get(0);
        List<CalendarDate> mappedDates = List.of(
                CalendarDate.builder().calDate(LocalDate.of(2026, 2, 1)).dayType(DayType.HOLIDAY_WORK).note("Out of range").build());
        when(calendarDateMapper.toEntities(eq(List.of(singleRequest)))).thenReturn(mappedDates);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> calendarDateService.createCalendarDates(List.of(singleRequest), calendar));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_DATE_OUT_OF_RANGE, ex.getMessage());
        verify(calendarDateRepository, never()).saveAll(any());
    }

    @Test
    void createCalendarDates_badRequestWhenDayTypeInvalid() {
        CompanyCalendarDateRequest singleRequest = requests.get(0);
        List<CalendarDate> mappedDates = List.of(
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 1)).dayType(null).note("Missing day type").build());
        when(calendarDateMapper.toEntities(eq(List.of(singleRequest)))).thenReturn(mappedDates);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> calendarDateService.createCalendarDates(List.of(singleRequest), calendar));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_DAY_TYPE_INVALID, ex.getMessage());
        verify(calendarDateRepository, never()).saveAll(any());
    }

    @Test
    void createCalendarDates_badRequestWhenNoteInvalid() {
        CompanyCalendarDateRequest singleRequest = requests.get(0);
        List<CalendarDate> mappedDates = List.of(
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 1)).dayType(DayType.HOLIDAY_WORK).note(" ").build());
        when(calendarDateMapper.toEntities(eq(List.of(singleRequest)))).thenReturn(mappedDates);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> calendarDateService.createCalendarDates(List.of(singleRequest), calendar));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_DATE_NOTE_INVALID, ex.getMessage());
        verify(calendarDateRepository, never()).saveAll(any());
    }

    @Test
    void getCompanyCalendarDates_success() {
        CalendarDate firstDate = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 1))
                .dayType(DayType.HOLIDAY_WORK)
                .note("New Year holiday")
                .build();
        CompanyCalendarDateResponse firstResponse = new CompanyCalendarDateResponse(
                LocalDate.of(2026, 1, 1),
                DayType.HOLIDAY_WORK,
                "New Year holiday");

        when(calendarDateRepository.findByCalendarCodeAndCompanyCodeAndDateRange(
                "CCA-000001",
                "CMP-001",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(firstDate));
        when(calendarDateMapper.toResponse(firstDate)).thenReturn(firstResponse);

        List<CompanyCalendarDateResponse> result = calendarDateService.getCompanyCalendarDates(calendar, 2026);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("New Year holiday", result.get(0).getNote());
    }

    @Test
    void getCompanyCalendarDates_badRequestWhenYearInvalid() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> calendarDateService.getCompanyCalendarDates(calendar, 0));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_YEAR_INVALID, ex.getMessage());
        verify(calendarDateRepository, never()).findByCalendarCodeAndCompanyCodeAndDateRange(any(), any(), any(), any());
    }
}
