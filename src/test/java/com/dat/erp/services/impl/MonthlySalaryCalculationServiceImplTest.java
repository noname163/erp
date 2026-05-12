package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.salary.calculation.CalculateSalaryDetailsStep;
import com.dat.erp.services.salary.calculation.CalculateStandardMoneyPerHourStep;
import com.dat.erp.services.salary.calculation.FinalizeMonthlySalaryResponseStep;
import com.dat.erp.services.salary.calculation.LoadPayrollDataStep;
import com.dat.erp.services.salary.calculation.ResolveActualWorkingHoursStep;
import com.dat.erp.services.salary.calculation.ResolveExpectedWorkingHoursStep;
import com.dat.erp.systemconfigs.CustomUserDetails;

class MonthlySalaryCalculationServiceImplTest {

    @Mock
    private SecurityContextService securityContextService;
    @Mock
    private EmployeeSalaryRepository employeeSalaryRepository;
    @Mock
    private EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    @Mock
    private DailyWorkRepository dailyWorkRepository;
    @Mock
    private EmployeePayrollPolicyService employeePayrollPolicyService;
    @Mock
    private CalendarDateService calendarDateService;

    private MonthlySalaryCalculationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MonthlySalaryCalculationServiceImpl(securityContextService, List.of(
                new FinalizeMonthlySalaryResponseStep(),
                new CalculateSalaryDetailsStep(),
                new CalculateStandardMoneyPerHourStep(),
                new ResolveActualWorkingHoursStep(dailyWorkRepository),
                new ResolveExpectedWorkingHoursStep(calendarDateService),
                new LoadPayrollDataStep(employeeSalaryRepository, employeeSalaryDetailRepository, employeePayrollPolicyService)));
        Account account = new Account();
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setCode("ESL-1");
        employeeSalary.setCurrency("MMK");
        UserProfile userProfile = new UserProfile();
        userProfile.setCode("EMP001");
        employeeSalary.setUserProfile(userProfile);
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 31))).thenReturn(Optional.of(employeeSalary));

        PayrollPolicy payrollPolicy = new PayrollPolicy();
        payrollPolicy.setStandardQuantityPerDay(8);
        when(employeePayrollPolicyService.getCompanyPoliciesByEmployeeCodesAndDate(List.of("EMP001"),
                LocalDate.of(2026, 3, 31))).thenReturn(Map.of("EMP001", payrollPolicy));

        when(calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth("CMP-1", YearMonth.of(2026, 3)))
                .thenReturn(Map.of(DayType.NORMAL, 20));
    }

    @Test
    void calculateEmployeeMonthlySalary_equalHourBranchUsesFullAmountSum() {
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(work(DayType.NORMAL, "160")));

        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("BASE", SalaryCalculateMethod.FIXED, null, DayType.NORMAL, "1000", true, true),
                        detail("BONUS", SalaryCalculateMethod.PLUS, null, DayType.NORMAL, "100", false, false)));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("1100.0000"), response.getFinalSalary());
        assertEquals(new BigDecimal("160"), response.getActualWorkingHourPerMonth());
        assertEquals("MMK", response.getAuditTrail().get(0).getUnit());
        assertEquals(new BigDecimal("1100.0000"), response.getAuditTrail().get(0).getTotalAmount());
    }

    @Test
    void calculateEmployeeMonthlySalary_unequalHourBranchUsesHourlyModelAndDependency() {
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(work(DayType.NORMAL, "120"), work(DayType.WEEKEND_WORK, "20")));

        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(
                        detail("BASE", SalaryCalculateMethod.PLUS, null, DayType.NORMAL, "100", true, true),
                        detail("BONUS", SalaryCalculateMethod.PERCENT, "BASE", DayType.NORMAL, "10", false, false),
                        detail("WKND", SalaryCalculateMethod.MINUS, null, DayType.WEEKEND_WORK, "5", false, false)));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("200.0000"), response.getFinalSalary());
        assertEquals(new BigDecimal("0.625000000000"), response.getStandardMoneyPerHour());
        assertEquals(new BigDecimal("200.0000"), response.getAuditTrail().get(0).getTotalAmount());
    }

    @Test
    void calculateEmployeeMonthlySalary_throwsWhenExpectedHoursZero() {
        when(calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth("CMP-1", YearMonth.of(2026, 3)))
                .thenReturn(Map.of());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3)));

        assertEquals(Messages.ERROR_PAYROLL_EXPECTED_WORKING_HOURS_INVALID, ex.getMessage());
    }

    @Test
    void calculateEmployeeMonthlySalary_throwsWhenDayTypeMappingMissing() {
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(work(DayType.NORMAL, "100")));

        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("BASE", SalaryCalculateMethod.PLUS, null, DayType.WEEKEND_WORK, "100", true,
                        true)));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3)));

        assertEquals(String.format(Messages.ERROR_PAYROLL_DAY_TYPE_HOURS_MISSING, DayType.WEEKEND_WORK), ex.getMessage());
    }

    @Test
    void calculateEmployeeMonthlySalary_throwsWhenCircularDependency() {
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(work(DayType.NORMAL, "100")));

        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(
                        detail("A", SalaryCalculateMethod.PERCENT, "B", DayType.NORMAL, "10", false, false),
                        detail("B", SalaryCalculateMethod.PERCENT, "A", DayType.NORMAL, "5", false, false)));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3)));

        assertTrue(ex.getMessage().startsWith("Circular dependency detected"));
    }

    private DailyWork work(DayType dayType, String hours) {
        DailyWork dailyWork = new DailyWork();
        dailyWork.setWorkType(dayType);
        dailyWork.setHoursWorked(new BigDecimal(hours));
        return dailyWork;
    }

    private EmployeeSalaryDetail detail(String code, SalaryCalculateMethod method, String dependenceCode, DayType dayType,
            String amount, boolean isDeduct, boolean isFixed) {
        Salary salary = new Salary();
        salary.setCode(code);
        salary.setCalculateMethod(method);
        salary.setIsDeduct(isDeduct);

        EmployeeSalaryDetail detail = new EmployeeSalaryDetail();
        detail.setSalary(salary);
        detail.setAmount(amount);
        detail.setDayType(dayType);
        detail.setIsFixed(isFixed);
        if (dependenceCode != null) {
            Salary dependenceSalary = new Salary();
            dependenceSalary.setCode(dependenceCode);
            detail.setDependenceCode(dependenceSalary);
        }
        return detail;
    }
}
