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
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
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
import com.dat.erp.repositories.projections.CalendarDateDayTypeCountProjection;
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

    @Captor
    private ArgumentCaptor<Iterable<CalendarDate>> deleteCaptor;

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
        com.dat.erp.testutils.EntityTestData.setCode(calendar, "CCA-000001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(calendar, "CMP-001");

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
        com.dat.erp.testutils.EntityTestData.setCode(account, "ACC-001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(account, "CMP-001");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(account.getCompanyCode());
    }

    @Test
    void createCalendarDates_success() {
        List<CalendarDate> mappedDates = List.of(
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 1)).dayType(DayType.HOLIDAY_WORK).note("New Year holiday").build(),
                CalendarDate.builder().calDate(LocalDate.of(2026, 1, 4)).dayType(DayType.WEEKEND_WORK).note("First weekend").build());
        com.dat.erp.testutils.EntityTestData.setCompanyCode(mappedDates.get(0), "CMP-001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(mappedDates.get(1), "CMP-001");
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
    void replaceCalendarDates_success() {
        CalendarDate updatedRequestedDate = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 1))
                .dayType(DayType.HOLIDAY)
                .note("Updated New Year holiday")
                .build();
        CalendarDate newRequestedDate = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 5))
                .dayType(DayType.NORMAL)
                .note("Back to work")
                .build();
        CalendarDate existingDateToUpdate = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 1))
                .dayType(DayType.HOLIDAY_WORK)
                .note("Old note")
                .build();
        com.dat.erp.testutils.EntityTestData.setCode(existingDateToUpdate, "CAD-000001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(existingDateToUpdate, "CMP-001");
        CalendarDate existingDateToDelete = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 4))
                .dayType(DayType.WEEKEND_WORK)
                .note("Remove this date")
                .build();
        com.dat.erp.testutils.EntityTestData.setCode(existingDateToDelete, "CAD-000002");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(existingDateToDelete, "CMP-001");

        when(calendarDateMapper.toEntities(requests)).thenReturn(List.of(updatedRequestedDate, newRequestedDate));
        when(calendarDateRepository.findByCalendarCodeAndCompanyCode("CCA-000001", "CMP-001"))
                .thenReturn(List.of(existingDateToUpdate, existingDateToDelete));
        when(codeGenerator.nextCode("CAD-")).thenReturn("CAD-000003");
        when(calendarDateRepository.saveAll(ArgumentMatchers.<List<CalendarDate>>any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<CalendarDate> result = calendarDateService.replaceCalendarDates(requests, calendar);

        assertEquals(2, result.size());
        assertEquals("CAD-000001", result.get(0).getCode());
        assertEquals(DayType.HOLIDAY, result.get(0).getDayType());
        assertEquals("Updated New Year holiday", result.get(0).getNote());
        assertEquals("CAD-000003", result.get(1).getCode());
        assertEquals(calendar, result.get(1).getCalendar());

        verify(calendarDateRepository).deleteAll(deleteCaptor.capture());
        List<CalendarDate> deletedDates = new java.util.ArrayList<>();
        deleteCaptor.getValue().forEach(deletedDates::add);
        assertEquals(1, deletedDates.size());
        assertEquals(existingDateToDelete, deletedDates.get(0));
        verify(calendarDateRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
        assertEquals(LocalDate.of(2026, 1, 5), captor.getValue().get(0).getCalDate());
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

        when(calendarDateRepository.findByCalendarCodeAndCompanyCode(
                "CCA-000001",
                "CMP-001"))
                .thenReturn(List.of(firstDate));
        when(calendarDateMapper.toResponse(firstDate)).thenReturn(firstResponse);

        List<CompanyCalendarDateResponse> result = calendarDateService.getCompanyCalendarDates(calendar);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("New Year holiday", result.get(0).getNote());
    }

    @Test
    void getCalendarDateTotalsByCompanyCodeAndMonth_returnsCountMapByDayType() {
        CalendarDateDayTypeCountProjection holidayProjection = org.mockito.Mockito.mock(CalendarDateDayTypeCountProjection.class);
        when(holidayProjection.getDayType()).thenReturn(DayType.HOLIDAY_WORK);
        when(holidayProjection.getTotalDates()).thenReturn(2L);

        CalendarDateDayTypeCountProjection weekendProjection = org.mockito.Mockito.mock(CalendarDateDayTypeCountProjection.class);
        when(weekendProjection.getDayType()).thenReturn(DayType.WEEKEND_WORK);
        when(weekendProjection.getTotalDates()).thenReturn(4L);

        when(calendarDateRepository.countByCompanyCodeAndDateRangeGroupByDayType(
                "CMP-001",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)))
                .thenReturn(List.of(holidayProjection, weekendProjection));

        Map<DayType, Integer> result = calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth(
                " CMP-001 ",
                YearMonth.of(2026, 1));

        assertEquals(2, result.size());
        assertEquals(2, result.get(DayType.HOLIDAY_WORK));
        assertEquals(4, result.get(DayType.WEEKEND_WORK));
    }
}
