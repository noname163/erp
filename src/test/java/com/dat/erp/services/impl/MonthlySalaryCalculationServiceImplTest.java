package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import com.dat.erp.constants.PayRateDayType;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.constants.SalaryBasisType;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.entities.Account;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeKpiResult;
import com.dat.erp.entities.EmployeeProductionResult;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.PayRateRule;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeKpiResultRepository;
import com.dat.erp.repositories.customrepositories.EmployeeProductionResultRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayRateRuleRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.salary.calculation.CalculateSalaryDetailsStep;
import com.dat.erp.services.salary.calculation.CalculateStandardMoneyPerHourStep;
import com.dat.erp.services.salary.calculation.FinalizeMonthlySalaryResponseStep;
import com.dat.erp.services.salary.calculation.LoadPayrollDataStep;
import com.dat.erp.services.salary.calculation.ResolveActualWorkingHoursStep;
import com.dat.erp.services.salary.calculation.ResolveExpectedWorkingHoursStep;
import com.dat.erp.services.salary.calculation.ResolveSalaryBasisStep;
import com.dat.erp.services.salary.calculation.amount.DivideSalaryAmountCalculationStrategy;
import com.dat.erp.services.salary.calculation.amount.FixedSalaryAmountCalculationStrategy;
import com.dat.erp.services.salary.calculation.amount.FormulaSalaryAmountCalculationStrategy;
import com.dat.erp.services.salary.calculation.amount.MinusSalaryAmountCalculationStrategy;
import com.dat.erp.services.salary.calculation.amount.PercentSalaryAmountCalculationStrategy;
import com.dat.erp.services.salary.calculation.amount.PlusSalaryAmountCalculationStrategy;
import com.dat.erp.services.salary.calculation.amount.SalaryAmountCalculationStrategyFactory;
import com.dat.erp.services.salary.calculation.basis.KpiSalaryBasisCalculationStrategy;
import com.dat.erp.services.salary.calculation.basis.HourlySalaryBasisCalculationStrategy;
import com.dat.erp.services.salary.calculation.basis.MonthlySalaryBasisCalculationStrategy;
import com.dat.erp.services.salary.calculation.basis.ProductSalaryBasisCalculationStrategy;
import com.dat.erp.services.salary.calculation.basis.QuantitySalaryBasisCalculationStrategy;
import com.dat.erp.services.salary.calculation.basis.SalaryBasisCalculationStrategyFactory;
import com.dat.erp.services.salary.calculation.basis.WorkingHourSalaryBasisCalculationStrategy;
import com.dat.erp.services.salary.calculation.detail.SalaryDetailDependencyEvaluator;
import com.dat.erp.services.salary.calculation.hour.ActualWorkingHourStrategyFactory;
import com.dat.erp.services.salary.calculation.hour.ExpectedWorkingHourStrategyFactory;
import com.dat.erp.services.salary.calculation.hour.HolidayWorkActualWorkingHourStrategy;
import com.dat.erp.services.salary.calculation.hour.NormalActualWorkingHourStrategy;
import com.dat.erp.services.salary.calculation.hour.NormalExpectedWorkingHourStrategy;
import com.dat.erp.services.salary.calculation.hour.WeekendWorkActualWorkingHourStrategy;
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
    @Mock
    private PayRateRuleRepository payRateRuleRepository;
    @Mock
    private EmployeeProductionResultRepository employeeProductionResultRepository;
    @Mock
    private EmployeeKpiResultRepository employeeKpiResultRepository;

    private MonthlySalaryCalculationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SalaryAmountCalculationStrategyFactory amountStrategyFactory = new SalaryAmountCalculationStrategyFactory(List.of(
                new PlusSalaryAmountCalculationStrategy(),
                new MinusSalaryAmountCalculationStrategy(),
                new FixedSalaryAmountCalculationStrategy(),
                new FormulaSalaryAmountCalculationStrategy(),
                new PercentSalaryAmountCalculationStrategy(),
                new DivideSalaryAmountCalculationStrategy()));
        SalaryDetailDependencyEvaluator dependencyEvaluator = new SalaryDetailDependencyEvaluator(amountStrategyFactory);
        ExpectedWorkingHourStrategyFactory expectedHourStrategyFactory = new ExpectedWorkingHourStrategyFactory(List.of(
                new NormalExpectedWorkingHourStrategy()));
        ActualWorkingHourStrategyFactory actualHourStrategyFactory = new ActualWorkingHourStrategyFactory(List.of(
                new NormalActualWorkingHourStrategy(),
                new WeekendWorkActualWorkingHourStrategy(),
                new HolidayWorkActualWorkingHourStrategy()));
        SalaryBasisCalculationStrategyFactory basisStrategyFactory = new SalaryBasisCalculationStrategyFactory(List.of(
                new MonthlySalaryBasisCalculationStrategy(),
                new HourlySalaryBasisCalculationStrategy(),
                new WorkingHourSalaryBasisCalculationStrategy(),
                new QuantitySalaryBasisCalculationStrategy(),
                new ProductSalaryBasisCalculationStrategy(),
                new KpiSalaryBasisCalculationStrategy()));

        service = new MonthlySalaryCalculationServiceImpl(securityContextService, List.of(
                new FinalizeMonthlySalaryResponseStep(),
                new CalculateSalaryDetailsStep(dependencyEvaluator),
                new ResolveSalaryBasisStep(basisStrategyFactory),
                new CalculateStandardMoneyPerHourStep(),
                new ResolveActualWorkingHoursStep(dailyWorkRepository),
                new ResolveExpectedWorkingHoursStep(calendarDateService, expectedHourStrategyFactory),
                new LoadPayrollDataStep(employeeSalaryRepository, employeeSalaryDetailRepository,
                        employeePayrollPolicyService, payRateRuleRepository, employeeProductionResultRepository,
                        employeeKpiResultRepository)));
        Account account = new Account();
        com.dat.erp.testutils.EntityTestData.setCompanyCode(account, "CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));
        when(securityContextService.getCurrentCompanyCode()).thenReturn(account.getCompanyCode());

        EmployeeSalary employeeSalary = new EmployeeSalary();
        com.dat.erp.testutils.EntityTestData.setCode(employeeSalary, "ESL-1");
        employeeSalary.setCurrency("MMK");
        UserProfile userProfile = new UserProfile();
        com.dat.erp.testutils.EntityTestData.setCode(userProfile, "EMP001");
        employeeSalary.setUserProfile(userProfile);
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 31))).thenReturn(Optional.of(employeeSalary));

        PayrollPolicy payrollPolicy = new PayrollPolicy();
        payrollPolicy.setStandardQuantityPerDay(8);
        when(employeePayrollPolicyService.getCompanyPoliciesByEmployeeCodesAndDate(List.of("EMP001"),
                LocalDate.of(2026, 3, 31))).thenReturn(Map.of("EMP001", payrollPolicy));

        when(calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth("CMP-1", YearMonth.of(2026, 3)))
                .thenReturn(Map.of(DayType.NORMAL, 20));
        when(payRateRuleRepository.findActiveByPolicyCodeAndCompanyCodeAndPeriod(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(employeeProductionResultRepository.findApprovedByEmployeeAndDateRange(any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(employeeKpiResultRepository.findApprovedByEmployeeAndPeriod(any(), any(), any(), any()))
                .thenReturn(List.of());
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
        assertEquals(SalaryBasisType.WORKING_HOUR, response.getSalaryBasisType());
        assertEquals("HOUR", response.getBasisUnit());
        assertEquals(new BigDecimal("160"), response.getActualBasisValue());
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

    @Test
    void calculateEmployeeMonthlySalary_paidLeaveCountsAsPaidWorkingTime() {
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(work(DayType.NORMAL, "152"), work(DayType.PTO_PAID, "8")));
        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("BASE", SalaryCalculateMethod.FIXED, null, DayType.NORMAL, "1600", true, true)));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("1600.0000"), response.getFinalSalary());
        assertEquals(new BigDecimal("8"), response.getPaidLeaveHours());
        assertEquals(new BigDecimal("160"), response.getActualWorkingHourPerMonth());
    }

    @Test
    void calculateEmployeeMonthlySalary_unpaidLeaveReducesPaidWorkingTime() {
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 31))).thenReturn(Optional.of(employeeSalary(SalaryBasisType.HOURLY)));
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(work(DayType.NORMAL, "152"), work(DayType.UNPAID_LEAVE, "8")));
        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("BASE", SalaryCalculateMethod.FIXED, null, DayType.NORMAL, "10", true, true)));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("1520.0000"), response.getFinalSalary());
        assertEquals(new BigDecimal("8"), response.getUnpaidLeaveHours());
        assertEquals(new BigDecimal("152"), response.getActualWorkingHourPerMonth());
    }

    @Test
    void calculateEmployeeMonthlySalary_lateArrivalReducesPaidWorkingTime() {
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 31))).thenReturn(Optional.of(employeeSalary(SalaryBasisType.HOURLY)));
        DailyWork lateWork = work(DayType.NORMAL, "8");
        lateWork.setLateMinutes(60);
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(lateWork));
        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("BASE", SalaryCalculateMethod.FIXED, null, DayType.NORMAL, "10", true, true)));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("70.0000"), response.getFinalSalary());
        assertEquals(new BigDecimal("1.000000000000"), response.getLateEarlyDeductionHours());
        assertEquals(new BigDecimal("7.000000000000"), response.getActualWorkingHourPerMonth());
    }

    @Test
    void calculateEmployeeMonthlySalary_hourlyHolidayWorkUsesMultiplier() {
        EmployeeSalary hourlySalary = employeeSalary(SalaryBasisType.HOURLY);
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 31))).thenReturn(Optional.of(hourlySalary));
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                        .thenReturn(List.of(work(DayType.NORMAL, "8"), work(DayType.HOLIDAY_WORK, "4")));
        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("BASE", SalaryCalculateMethod.FIXED, null, DayType.NORMAL, "10", true, true)));
        when(payRateRuleRepository.findActiveByPolicyCodeAndCompanyCodeAndPeriod(any(), any(), any(), any()))
                .thenReturn(List.of(payRateRule(PayRateDayType.HOLIDAY, "2")));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("160.0000"), response.getFinalSalary());
        assertEquals(SalaryBasisType.HOURLY, response.getSalaryBasisType());
    }

    @Test
    void calculateEmployeeMonthlySalary_productUsesApprovedQuantityTimesRate() {
        EmployeeSalary productSalary = employeeSalary(SalaryBasisType.PRODUCT);
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 31))).thenReturn(Optional.of(productSalary));
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31))).thenReturn(List.of());
        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("PIECE", SalaryCalculateMethod.FIXED, null, DayType.NORMAL, "5", true, true)));
        EmployeeProductionResult productionResult = new EmployeeProductionResult();
        productionResult.setProductCode("ITEM-1");
        productionResult.setQuantity(new BigDecimal("10"));
        when(employeeProductionResultRepository.findApprovedByEmployeeAndDateRange(any(), any(), any(), any(), any()))
                .thenReturn(List.of(productionResult));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("50.0000"), response.getFinalSalary());
        assertEquals(SalaryBasisType.PRODUCT, response.getSalaryBasisType());
        assertEquals(new BigDecimal("10"), response.getActualBasisValue());
    }

    @Test
    void calculateEmployeeMonthlySalary_kpiUsesApprovedScoreTimesRate() {
        EmployeeSalary kpiSalary = employeeSalary(SalaryBasisType.KPI);
        when(employeeSalaryRepository.findFirstActiveByEmployeeCodeAndCompanyCodeAndDate("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 31))).thenReturn(Optional.of(kpiSalary));
        when(dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange("EMP001", "CMP-1",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31))).thenReturn(List.of());
        when(employeeSalaryDetailRepository.findForPayrollByEmployeeSalaryCodeAndCompanyCode("ESL-1", "CMP-1"))
                .thenReturn(List.of(detail("KPI", SalaryCalculateMethod.FIXED, null, DayType.NORMAL, "2", true, true)));
        EmployeeKpiResult kpiResult = new EmployeeKpiResult();
        kpiResult.setKpiCode("KPI-1");
        kpiResult.setScore(new BigDecimal("80"));
        when(employeeKpiResultRepository.findApprovedByEmployeeAndPeriod(any(), any(), any(), any()))
                .thenReturn(List.of(kpiResult));

        MonthlySalaryCalculationResponse response = service.calculateEmployeeMonthlySalary("EMP001", YearMonth.of(2026, 3));

        assertEquals(new BigDecimal("160.0000"), response.getFinalSalary());
        assertEquals(SalaryBasisType.KPI, response.getSalaryBasisType());
        assertEquals(new BigDecimal("80"), response.getActualBasisValue());
    }

    private DailyWork work(DayType dayType, String hours) {
        DailyWork dailyWork = new DailyWork();
        dailyWork.setWorkType(dayType);
        dailyWork.setHoursWorked(new BigDecimal(hours));
        return dailyWork;
    }

    private EmployeeSalary employeeSalary(SalaryBasisType salaryBasisType) {
        EmployeeSalary employeeSalary = new EmployeeSalary();
        com.dat.erp.testutils.EntityTestData.setCode(employeeSalary, "ESL-1");
        employeeSalary.setCurrency("MMK");
        employeeSalary.setSalaryBasisType(salaryBasisType);
        UserProfile userProfile = new UserProfile();
        com.dat.erp.testutils.EntityTestData.setCode(userProfile, "EMP001");
        employeeSalary.setUserProfile(userProfile);
        return employeeSalary;
    }

    private PayRateRule payRateRule(PayRateDayType dayType, String multiplier) {
        PayRateRule payRateRule = new PayRateRule();
        payRateRule.setDayType(dayType);
        payRateRule.setMultiplier(new BigDecimal(multiplier));
        return payRateRule;
    }

    private EmployeeSalaryDetail detail(String code, SalaryCalculateMethod method, String dependenceCode, DayType dayType,
            String amount, boolean isDeduct, boolean isFixed) {
        Salary salary = new Salary();
        com.dat.erp.testutils.EntityTestData.setCode(salary, code);
        salary.setCalculateMethod(method);
        salary.setIsDeduct(isDeduct);

        EmployeeSalaryDetail detail = new EmployeeSalaryDetail();
        detail.setSalary(salary);
        detail.setAmount(amount);
        detail.setDayType(dayType);
        detail.setIsFixed(isFixed);
        if (dependenceCode != null) {
            Salary dependenceSalary = new Salary();
            com.dat.erp.testutils.EntityTestData.setCode(dependenceSalary, dependenceCode);
            detail.setDependenceCode(dependenceSalary);
        }
        return detail;
    }
}
