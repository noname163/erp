package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.dto.request.CompanyCalendarDateRequest;
import com.dat.erp.dto.request.CompanyCalendarRequest;
import com.dat.erp.dto.response.CompanyCalendarDateResponse;
import com.dat.erp.dto.response.CompanyCalendarListResponse;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.CompanyCalendarResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.mapper.interfaces.CompanyCalendarMapper;
import com.dat.erp.repositories.customrepositories.CompanyCalendarRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class CompanyCalendarServiceImplTest {

    @Mock
    private CompanyCalendarRepository companyCalendarRepository;

    @Mock
    private CalendarDateService calendarDateService;

    @Mock
    private CompanyCalendarMapper companyCalendarMapper;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private CompanyCalendarServiceImpl companyCalendarService;

    private CompanyCalendarRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        CompanyCalendarDateRequest firstDate = new CompanyCalendarDateRequest();
        firstDate.setCalDate(LocalDate.of(2026, 1, 1));
        firstDate.setDayType(DayType.HOLIDAY_WORK);
        firstDate.setNote("New Year holiday");

        CompanyCalendarDateRequest secondDate = new CompanyCalendarDateRequest();
        secondDate.setCalDate(LocalDate.of(2026, 1, 4));
        secondDate.setDayType(DayType.WEEKEND_WORK);
        secondDate.setNote("First weekend");

        request = new CompanyCalendarRequest();
        request.setName("  2026 Standard Calendar  ");
        request.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        request.setEffectiveTo(LocalDate.of(2026, 1, 31));
        request.setDates(List.of(firstDate, secondDate));
        request.setRegion("  APAC  ");
        request.setTimeZone(" Asia/Bangkok ");
        request.setNote("  Main regional calendar  ");

        Account account = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(account, "ACC-001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(account, "CMP-001");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(account.getCompanyCode());
    }

    @Test
    void createCompanyCalendar_success() {
        CompanyCalendar mappedCalendar = CompanyCalendar.builder()
                .name("2026 Standard Calendar")
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .region("APAC")
                .timeZone("Asia/Bangkok")
                .note("Main regional calendar")
                .build();
        CalendarDate secondSavedDate = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 4))
                .dayType(DayType.WEEKEND_WORK)
                .note("First weekend")
                .build();
        com.dat.erp.testutils.EntityTestData.setCode(secondSavedDate, "CAD-000002");
        CalendarDate firstSavedDate = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 1))
                .dayType(DayType.HOLIDAY_WORK)
                .note("New Year holiday")
                .build();
        com.dat.erp.testutils.EntityTestData.setCode(firstSavedDate, "CAD-000001");
        List<CalendarDate> savedDates = List.of(secondSavedDate, firstSavedDate);
        CompanyCalendarResponse mappedResponse = new CompanyCalendarResponse(
                "CCA-000001",
                "2026 Standard Calendar",
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                "APAC",
                "Asia/Bangkok",
                "Main regional calendar",
                List.of(
                        new CompanyCalendarDateResponse(LocalDate.of(2026, 1, 1), DayType.HOLIDAY_WORK, "New Year holiday"),
                        new CompanyCalendarDateResponse(LocalDate.of(2026, 1, 4), DayType.WEEKEND_WORK, "First weekend")));

        when(companyCalendarMapper.toEntity(request)).thenReturn(mappedCalendar);
        com.dat.erp.testutils.EntityTestData.setCompanyCode(mappedCalendar, "CMP-001");
        when(codeGenerator.nextCode("CCA-")).thenReturn("CCA-000001");
        when(companyCalendarRepository.save(any(CompanyCalendar.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(calendarDateService.createCalendarDates(eq(request.getDates()), any(CompanyCalendar.class))).thenReturn(savedDates);
        when(companyCalendarMapper.toResponse(any(CompanyCalendar.class))).thenReturn(mappedResponse);

        CompanyCalendarResponse response = companyCalendarService.createCompanyCalendar(request);

        assertNotNull(response);
        assertEquals("CCA-000001", response.getCode());
        assertEquals("2026 Standard Calendar", response.getName());
        assertEquals("APAC", response.getRegion());
        assertEquals("Asia/Bangkok", response.getTimeZone());
        assertEquals("Main regional calendar", response.getNote());
        assertEquals(2, response.getDates().size());
        assertEquals(LocalDate.of(2026, 1, 1), response.getDates().get(0).getCalDate());
        assertEquals(DayType.HOLIDAY_WORK, response.getDates().get(0).getDayType());
        assertEquals("New Year holiday", response.getDates().get(0).getNote());

        ArgumentCaptor<CompanyCalendar> calendarCaptor = ArgumentCaptor.forClass(CompanyCalendar.class);
        verify(companyCalendarRepository).save(calendarCaptor.capture());
        assertEquals("CMP-001", calendarCaptor.getValue().getCompanyCode());
        verify(calendarDateService).createCalendarDates(eq(request.getDates()), eq(calendarCaptor.getValue()));
        verify(companyCalendarMapper).toResponse(calendarCaptor.getValue());
    }

    @Test
    void createCompanyCalendar_badRequestWhenEffectiveDatesInvalid() {
        request.setEffectiveFrom(LocalDate.of(2026, 2, 1));
        request.setEffectiveTo(LocalDate.of(2026, 1, 1));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> companyCalendarService.createCompanyCalendar(request));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_EFFECTIVE_DATES_INVALID, ex.getMessage());
        verify(companyCalendarRepository, never()).save(any());
    }

    @Test
    void createCompanyCalendar_badRequestWhenNameInvalid() {
        CompanyCalendar mappedCalendar = CompanyCalendar.builder()
                .name(" ")
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .region("APAC")
                .timeZone("Asia/Bangkok")
                .note("Main regional calendar")
                .build();
        when(companyCalendarMapper.toEntity(request)).thenReturn(mappedCalendar);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> companyCalendarService.createCompanyCalendar(request));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_NAME_INVALID, ex.getMessage());
        verify(companyCalendarRepository, never()).save(any(CompanyCalendar.class));
    }

    @Test
    void createCompanyCalendar_badRequestWhenTimeZoneInvalid() {
        CompanyCalendar mappedCalendar = CompanyCalendar.builder()
                .name("2026 Standard Calendar")
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .region("APAC")
                .timeZone("Mars/Phobos")
                .note("Main regional calendar")
                .build();
        when(companyCalendarMapper.toEntity(request)).thenReturn(mappedCalendar);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> companyCalendarService.createCompanyCalendar(request));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_TIME_ZONE_INVALID, ex.getMessage());
        verify(companyCalendarRepository, never()).save(any(CompanyCalendar.class));
    }

    @Test
    void updateCompanyCalendar_success() {
        CompanyCalendar existingCalendar = CompanyCalendar.builder()
                .name("2025 Standard Calendar")
                .effectiveFrom(LocalDate.of(2025, 1, 1))
                .effectiveTo(LocalDate.of(2025, 12, 31))
                .region("GLOBAL")
                .timeZone("UTC")
                .note("Old calendar")
                .build();
        com.dat.erp.testutils.EntityTestData.setCode(existingCalendar, "CCA-000001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(existingCalendar, "CMP-001");

        CalendarDate savedDate = CalendarDate.builder()
                .calDate(LocalDate.of(2026, 1, 1))
                .dayType(DayType.HOLIDAY_WORK)
                .note("New Year holiday")
                .build();
        com.dat.erp.testutils.EntityTestData.setCode(savedDate, "CAD-000001");
        CompanyCalendarResponse mappedResponse = new CompanyCalendarResponse(
                "CCA-000001",
                "2026 Standard Calendar",
                request.getEffectiveFrom(),
                request.getEffectiveTo(),
                "APAC",
                "Asia/Bangkok",
                "Main regional calendar",
                List.of(new CompanyCalendarDateResponse(LocalDate.of(2026, 1, 1), DayType.HOLIDAY_WORK, "New Year holiday")));

        when(companyCalendarRepository.findByCodeAndCompanyCodeAndIsDeletedFalse("CCA-000001", "CMP-001"))
                .thenReturn(Optional.of(existingCalendar));
        doAnswer(invocation -> {
            CompanyCalendar target = invocation.getArgument(1);
            target.setName("2026 Standard Calendar");
            target.setEffectiveFrom(request.getEffectiveFrom());
            target.setEffectiveTo(request.getEffectiveTo());
            target.setRegion("APAC");
            target.setTimeZone("Asia/Bangkok");
            target.setNote("Main regional calendar");
            return null;
        }).when(companyCalendarMapper).updateEntity(eq(request), eq(existingCalendar));
        when(companyCalendarRepository.save(existingCalendar)).thenReturn(existingCalendar);
        when(calendarDateService.replaceCalendarDates(request.getDates(), existingCalendar)).thenReturn(List.of(savedDate));
        when(companyCalendarMapper.toResponse(existingCalendar)).thenReturn(mappedResponse);

        CompanyCalendarResponse response = companyCalendarService.updateCompanyCalendar("CCA-000001", request);

        assertNotNull(response);
        assertEquals("CCA-000001", response.getCode());
        assertEquals("2026 Standard Calendar", response.getName());
        assertEquals("Asia/Bangkok", response.getTimeZone());
        verify(companyCalendarRepository).save(existingCalendar);
        verify(calendarDateService).replaceCalendarDates(request.getDates(), existingCalendar);
        assertEquals("2026 Standard Calendar", existingCalendar.getName());
        assertEquals(LocalDate.of(2026, 1, 1), existingCalendar.getEffectiveFrom());
        assertEquals(LocalDate.of(2026, 1, 31), existingCalendar.getEffectiveTo());
        assertEquals("APAC", existingCalendar.getRegion());
        assertEquals("Asia/Bangkok", existingCalendar.getTimeZone());
        assertEquals("Main regional calendar", existingCalendar.getNote());
        verify(companyCalendarMapper).updateEntity(request, existingCalendar);
    }

    @Test
    void updateCompanyCalendar_notFoundWhenCodeMissing() {
        when(companyCalendarRepository.findByCodeAndCompanyCodeAndIsDeletedFalse("CCA-404", "CMP-001"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> companyCalendarService.updateCompanyCalendar("CCA-404", request));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_NOT_FOUND, ex.getMessage());
        verify(companyCalendarRepository, never()).save(any(CompanyCalendar.class));
    }

    @Test
    void getCompanyCalendars_success() {
        CompanyCalendar calendar = CompanyCalendar.builder()
                .name("Bangkok Calendar")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 12, 31))
                .region("APAC")
                .timeZone("Asia/Bangkok")
                .note("Main regional calendar")
                .build();
        CompanyCalendarListResponse listResponse = new CompanyCalendarListResponse(
                "CCA-000001",
                "Bangkok Calendar",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "APAC",
                "Asia/Bangkok",
                "Main regional calendar",
                "ACC-001");

        when(companyCalendarRepository.searchByConditions(
                eq("CMP-001"),
                eq("Bangkok"),
                eq("APAC"),
                eq("Asia/Bangkok"),
                any()))
                .thenReturn(new PageImpl<>(List.of(calendar), PageRequest.of(0, 20), 1));
        when(companyCalendarMapper.toListResponse(calendar)).thenReturn(listResponse);

        PagedResponse<CompanyCalendarListResponse> response = companyCalendarService.getCompanyCalendars(
                " Bangkok ",
                " APAC ",
                " Asia/Bangkok ",
                0,
                20,
                null,
                "DESC");

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals("CCA-000001", response.getData().get(0).getCode());
        assertEquals(Messages.SUCCESS, response.getMessage());
    }

    @Test
    void getCompanyCalendarDates_success() {
        CompanyCalendar calendar = CompanyCalendar.builder()
                .name("Bangkok Calendar")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(LocalDate.of(2026, 12, 31))
                .region("APAC")
                .timeZone("Asia/Bangkok")
                .note("Main regional calendar")
                .build();
        com.dat.erp.testutils.EntityTestData.setCode(calendar, "CCA-000001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(calendar, "CMP-001");

        List<CompanyCalendarDateResponse> dateResponses = List.of(
                new CompanyCalendarDateResponse(LocalDate.of(2026, 1, 1), DayType.HOLIDAY_WORK, "New Year holiday"));

        when(companyCalendarRepository.findByCodeAndCompanyCodeAndIsDeletedFalse("CCA-000001", "CMP-001"))
                .thenReturn(Optional.of(calendar));
        when(calendarDateService.getCompanyCalendarDates(calendar)).thenReturn(dateResponses);

        List<CompanyCalendarDateResponse> response = companyCalendarService.getCompanyCalendarDates("CCA-000001");

        assertEquals(1, response.size());
        assertEquals("New Year holiday", response.get(0).getNote());
    }

    @Test
    void getCompanyCalendarDates_notFoundWhenCodeMissing() {
        when(companyCalendarRepository.findByCodeAndCompanyCodeAndIsDeletedFalse("CCA-404", "CMP-001"))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> companyCalendarService.getCompanyCalendarDates("CCA-404"));

        assertEquals(Messages.ERROR_COMPANY_CALENDAR_NOT_FOUND, ex.getMessage());
    }

    @Test
    void createCompanyCalendar_badRequestWhenCompanyMissing() {
        Account account = new Account();
        com.dat.erp.testutils.EntityTestData.setCode(account, "ACC-001");
        com.dat.erp.testutils.EntityTestData.setCompanyCode(account, " ");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(account.getCompanyCode());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> companyCalendarService.createCompanyCalendar(request));

        assertEquals(Messages.ERROR_CURRENT_USER_COMPANY_MISSING, ex.getMessage());
        verify(companyCalendarRepository, never()).save(any(CompanyCalendar.class));
    }
}
