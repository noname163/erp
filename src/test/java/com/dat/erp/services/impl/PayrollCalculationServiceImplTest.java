package com.dat.erp.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.constants.CalendarDayType;
import com.dat.erp.constants.LeavePaidMode;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayRateAppliesTo;
import com.dat.erp.constants.PayRateDayType;
import com.dat.erp.constants.PayrollAdjustmentType;
import com.dat.erp.constants.PayrollLineType;
import com.dat.erp.constants.PayrollProrationBasis;
import com.dat.erp.constants.PayrollRateRuleType;
import com.dat.erp.constants.PayrollSourceType;
import com.dat.erp.constants.PayrollSummaryStatus;
import com.dat.erp.constants.SalaryComponentType;
import com.dat.erp.entities.CalendarDate;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeePto;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.EmployeeSalaryDetail;
import com.dat.erp.entities.EmploymentAgreement;
import com.dat.erp.entities.PayRateRule;
import com.dat.erp.entities.PayrollAdjustment;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.Salary;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.entities.WorkScheduleDetail;
import com.dat.erp.services.PayrollEmployeeDraft;
import com.dat.erp.services.PayrollLineDraft;

class PayrollCalculationServiceImplTest {

    private static final LocalDate PERIOD_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate PERIOD_END = LocalDate.of(2026, 1, 31);

    private PayrollCalculationServiceImpl payrollCalculationService;
    private UserProfile userProfile;
    private WorkSchedule workSchedule;
    private List<WorkScheduleDetail> scheduleDetails;
    private CompanyCalendar companyCalendar;
    private PayrollPolicy payrollPolicy;
    private Salary baseSalary;
    private List<PayRateRule> defaultRateRules;

    @BeforeEach
    void setUp() {
        payrollCalculationService = new PayrollCalculationServiceImpl();
        userProfile = user("USR-001", LocalDate.of(2025, 12, 1), true);
        workSchedule = new WorkSchedule();
        workSchedule.setCode("WRS-001");
        workSchedule.setName("Standard");
        scheduleDetails = standardSchedule();
        companyCalendar = new CompanyCalendar();
        companyCalendar.setCode("CAL-001");
        companyCalendar.setName("Default");
        payrollPolicy = new PayrollPolicy();
        payrollPolicy.setCode("PPL-001");
        payrollPolicy.setProrationBasis(PayrollProrationBasis.CALENDAR_DAYS);
        payrollPolicy.setPayHolidayIfOff(true);
        payrollPolicy.setStandardHoursPerDay(8);
        payrollPolicy.setNightPremiumStart(LocalTime.of(19, 0));
        payrollPolicy.setNightPremiumEnd(LocalTime.of(23, 59, 59));
        payrollPolicy.setEffectiveFrom(PERIOD_START);
        payrollPolicy.setEffectiveTo(PERIOD_END);
        baseSalary = new Salary();
        baseSalary.setCode("SAL-BASE");
        baseSalary.setName("Base Salary");
        baseSalary.setIsDeduct(false);
        baseSalary.setComponentType(SalaryComponentType.BASE_SALARY);
        defaultRateRules = List.of(
                rateRule(PayrollRateRuleType.DAY_PREMIUM, PayRateDayType.HOLIDAY, "3.0"),
                rateRule(PayrollRateRuleType.DAY_PREMIUM, PayRateDayType.WEEKEND, "2.0"),
                rateRule(PayrollRateRuleType.OVERTIME, PayRateDayType.NORMAL, "1.5"),
                rateRule(PayrollRateRuleType.NIGHT_PREMIUM, PayRateDayType.NORMAL, "0.25"),
                rateRule(PayrollRateRuleType.NIGHT_PREMIUM, PayRateDayType.HOLIDAY, "0.25"),
                rateRule(PayrollRateRuleType.NIGHT_PREMIUM, PayRateDayType.WEEKEND, "0.25"));
    }

    @Test
    void calculate_fullMonthActiveEmployee_returnsFullMonthlySalary() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of());

        assertEquals(PayrollSummaryStatus.SUCCESS, draft.status());
        assertEquals(new BigDecimal("3100.00"), draft.grossAmount());
        assertEquals(new BigDecimal("0.00"), draft.deductionAmount());
        assertEquals(new BigDecimal("3100.00"), draft.netAmount());
    }

    @Test
    void calculate_employeeJoinsMidMonth_proratesSalaryFromHireDate() {
        UserProfile joinedMidMonth = user("USR-002", LocalDate.of(2026, 1, 16), true);

        PayrollEmployeeDraft draft = calculate(
                joinedMidMonth,
                List.of(),
                List.of(salaryRecord("ESL-001", LocalDate.of(2026, 1, 16), PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of());

        assertEquals(new BigDecimal("1600.00"), draft.grossAmount());
        assertEquals(new BigDecimal("1600.00"), draft.netAmount());
    }

    @Test
    void calculate_employeeResignsMidMonth_proratesSalaryUntilResignationDate() {
        EmploymentAgreement agreement = new EmploymentAgreement();
        agreement.setCode("EAG-001");
        agreement.setEffectiveFrom(PERIOD_START);
        agreement.setEffectiveTo(PERIOD_END);
        agreement.setResignationDate(LocalDate.of(2026, 1, 20));

        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(agreement),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of());

        assertEquals(new BigDecimal("2000.00"), draft.grossAmount());
        assertEquals(new BigDecimal("2000.00"), draft.netAmount());
    }

    @Test
    void calculate_unpaidLeaveDay_createsDeductionLine() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(unpaidLeave(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 10))),
                List.of());

        assertEquals(new BigDecimal("3100.00"), draft.grossAmount());
        assertEquals(new BigDecimal("100.00"), draft.deductionAmount());
        assertEquals(new BigDecimal("3000.00"), draft.netAmount());
        assertTrue(draft.lines().stream().anyMatch(line -> line.lineType() == PayrollLineType.DEDUCTION
                && line.sourceType() == PayrollSourceType.PTO
                && line.amount().compareTo(new BigDecimal("100.00")) == 0));
    }

    @Test
    void calculate_paidLeaveDay_doesNotCreateDeductionLine() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(paidLeave(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 10))),
                List.of());

        assertEquals(new BigDecimal("0.00"), draft.deductionAmount());
        assertEquals(new BigDecimal("3100.00"), draft.netAmount());
    }

    @Test
    void calculate_publicHolidayOff_keepsFullSalary() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(paidHoliday(LocalDate.of(2026, 1, 1))),
                List.of(),
                List.of());

        assertEquals(new BigDecimal("3100.00"), draft.grossAmount());
        assertEquals(new BigDecimal("3100.00"), draft.netAmount());
    }

    @Test
    void calculate_holidayWorked_usesHolidayMultiplier() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(paidHoliday(LocalDate.of(2026, 1, 1))),
                List.of(),
                List.of(dailyWork(LocalDate.of(2026, 1, 1), 1, null, null, null)));

        assertEquals(new BigDecimal("3400.00"), draft.grossAmount());
        assertLineAmount(draft.lines(), PayrollSourceType.HOLIDAY_PREMIUM, "300.00");
    }

    @Test
    void calculate_weekendWorked_usesWeekendMultiplier() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of(dailyWork(LocalDate.of(2026, 1, 3), 1, null, null, null)));

        assertEquals(new BigDecimal("3300.00"), draft.grossAmount());
        assertLineAmount(draft.lines(), PayrollSourceType.WEEKEND_PREMIUM, "200.00");
    }

    @Test
    void calculate_weekdayOt_addsOtPremium() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of(dailyWork(LocalDate.of(2026, 1, 5), 1, 2, null, null)));

        assertEquals(new BigDecimal("3137.50"), draft.grossAmount());
        assertLineAmount(draft.lines(), PayrollSourceType.OT, "37.50");
    }

    @Test
    void calculate_nightPremiumAfterSevenPm_addsNightPremium() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of(dailyWork(LocalDate.of(2026, 1, 5), 1, null, LocalDateTime.of(2026, 1, 5, 18, 0),
                        LocalDateTime.of(2026, 1, 5, 22, 0))));

        assertEquals(new BigDecimal("3109.38"), draft.grossAmount());
        assertLineAmount(draft.lines(), PayrollSourceType.NIGHT_PREMIUM, "9.38");
    }

    @Test
    void calculate_salaryChangeMidMonth_combinesSegmentAmounts() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(
                        salaryRecord("ESL-001", PERIOD_START, LocalDate.of(2026, 1, 15)),
                        salaryRecord("ESL-002", LocalDate.of(2026, 1, 16), PERIOD_END)),
                List.of(
                        salaryDetail("ESL-001", "3100"),
                        salaryDetail("ESL-002", "6200")),
                List.of(),
                List.of(),
                List.of());

        assertEquals(new BigDecimal("4700.00"), draft.grossAmount());
        assertEquals(2, draft.lines().stream().filter(line -> line.sourceType() == PayrollSourceType.SALARY).count());
    }

    @Test
    void calculate_approvedBonus_addsManualBonusLine() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of(),
                List.of(adjustment(PayrollAdjustmentType.BONUS, "500", false, "Bonus")));

        assertEquals(new BigDecimal("3600.00"), draft.grossAmount());
        assertLineAmount(draft.lines(), PayrollSourceType.BONUS, "500.00");
    }

    @Test
    void calculate_deductionAdjustment_reducesNetPay() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of(),
                List.of(adjustment(PayrollAdjustmentType.DEDUCTION, "250", false, "Penalty")));

        assertEquals(new BigDecimal("3100.00"), draft.grossAmount());
        assertEquals(new BigDecimal("250.00"), draft.deductionAmount());
        assertEquals(new BigDecimal("2850.00"), draft.netAmount());
        assertLineAmount(draft.lines(), PayrollSourceType.DEDUCTION, "250.00");
    }

    @Test
    void calculate_retroAdjustment_createsRetroLine() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(),
                List.of(),
                List.of(),
                List.of(adjustment(PayrollAdjustmentType.RETRO, "400", true, "Missed OT")));

        assertEquals(new BigDecimal("3500.00"), draft.grossAmount());
        PayrollLineDraft retroLine = draft.lines().stream()
                .filter(line -> line.sourceType() == PayrollSourceType.RETRO)
                .findFirst()
                .orElseThrow();
        assertEquals(PayrollLineType.RETRO, retroLine.lineType());
        assertTrue(retroLine.retro());
    }

    @Test
    void calculate_withoutSalaryRecord_returnsBlockingFailure() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());

        assertEquals(PayrollSummaryStatus.FAILED, draft.status());
        assertTrue(draft.hasBlockingIssue());
        assertEquals(Messages.ERROR_PAYROLL_NO_SALARY_RECORD, draft.issueMessage());
    }

    @Test
    void calculate_overlappingSalaryRecords_returnsBlockingFailure() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(
                        salaryRecord("ESL-001", PERIOD_START, LocalDate.of(2026, 1, 20)),
                        salaryRecord("ESL-002", LocalDate.of(2026, 1, 15), PERIOD_END)),
                List.of(
                        salaryDetail("ESL-001", "3100"),
                        salaryDetail("ESL-002", "6200")),
                List.of(),
                List.of(),
                List.of());

        assertEquals(PayrollSummaryStatus.FAILED, draft.status());
        assertEquals(Messages.ERROR_PAYROLL_OVERLAPPING_SALARY_RECORD, draft.issueMessage());
    }

    @Test
    void calculate_holidayOverlappingWeekend_usesHolidayRulePrecedence() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(paidHoliday(LocalDate.of(2026, 1, 4))),
                List.of(),
                List.of(dailyWork(LocalDate.of(2026, 1, 4), 1, null, null, null)));

        assertEquals(new BigDecimal("3400.00"), draft.grossAmount());
        assertLineAmount(draft.lines(), PayrollSourceType.HOLIDAY_PREMIUM, "300.00");
    }

    @Test
    void calculate_leaveOverlappingHoliday_doesNotDeductUnpaidLeave() {
        PayrollEmployeeDraft draft = calculate(
                userProfile,
                List.of(),
                List.of(salaryRecord("ESL-001", PERIOD_START, PERIOD_END)),
                List.of(salaryDetail("ESL-001", "3100")),
                List.of(paidHoliday(LocalDate.of(2026, 1, 1))),
                List.of(unpaidLeave(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 1))),
                List.of());

        assertEquals(new BigDecimal("0.00"), draft.deductionAmount());
        assertEquals(new BigDecimal("3100.00"), draft.netAmount());
    }

    private PayrollEmployeeDraft calculate(
            UserProfile user,
            List<EmploymentAgreement> agreements,
            List<EmployeeSalary> salaryRecords,
            List<EmployeeSalaryDetail> salaryDetails,
            List<CalendarDate> calendarDates,
            List<EmployeePto> ptoRecords,
            List<DailyWork> dailyWorks) {
        return calculate(user, agreements, salaryRecords, salaryDetails, calendarDates, ptoRecords, dailyWorks, List.of());
    }

    private PayrollEmployeeDraft calculate(
            UserProfile user,
            List<EmploymentAgreement> agreements,
            List<EmployeeSalary> salaryRecords,
            List<EmployeeSalaryDetail> salaryDetails,
            List<CalendarDate> calendarDates,
            List<EmployeePto> ptoRecords,
            List<DailyWork> dailyWorks,
            List<PayrollAdjustment> adjustments) {
        PayrollEmployeeDraft draft = payrollCalculationService.calculate(
                user,
                PERIOD_START,
                PERIOD_END,
                agreements,
                salaryRecords,
                salaryDetails,
                List.of(payrollPolicy),
                defaultRateRules,
                workSchedule,
                scheduleDetails,
                companyCalendar,
                calendarDates,
                dailyWorks,
                ptoRecords,
                adjustments);
        assertNotNull(draft);
        return draft;
    }

    private UserProfile user(String code, LocalDate hireDate, boolean active) {
        UserProfile userProfile = new UserProfile();
        userProfile.setCode(code);
        userProfile.setCompanyCode("CMP-001");
        userProfile.setHireDate(hireDate);
        userProfile.setIsActive(active);
        return userProfile;
    }

    private List<WorkScheduleDetail> standardSchedule() {
        return List.of(
                scheduleDetail(1, true),
                scheduleDetail(2, true),
                scheduleDetail(3, true),
                scheduleDetail(4, true),
                scheduleDetail(5, true),
                scheduleDetail(6, false),
                scheduleDetail(7, false));
    }

    private WorkScheduleDetail scheduleDetail(int dayOfWeek, boolean isWorkingDay) {
        WorkScheduleDetail detail = new WorkScheduleDetail();
        detail.setCode("WSD-" + dayOfWeek);
        detail.setDayOfWeek(dayOfWeek);
        detail.setIsWorkingDay(isWorkingDay);
        detail.setStartTime(LocalTime.of(9, 0));
        detail.setEndTime(LocalTime.of(17, 0));
        detail.setBreakMinutes(0);
        detail.setPaidBreak(false);
        detail.setFullDayThresholdMinutes(480);
        return detail;
    }

    private EmployeeSalary salaryRecord(String code, LocalDate effectiveFrom, LocalDate effectiveTo) {
        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setCode(code);
        employeeSalary.setCompanyCode("CMP-001");
        employeeSalary.setCurrency("VND");
        employeeSalary.setEffectiveFrom(effectiveFrom);
        employeeSalary.setEffectiveTo(effectiveTo);
        return employeeSalary;
    }

    private EmployeeSalaryDetail salaryDetail(String employeeSalaryCode, String amount) {
        EmployeeSalary employeeSalary = new EmployeeSalary();
        employeeSalary.setCode(employeeSalaryCode);
        Salary salary = new Salary();
        salary.setCode(baseSalary.getCode());
        salary.setName(baseSalary.getName());
        salary.setIsDeduct(false);
        salary.setComponentType(SalaryComponentType.BASE_SALARY);
        return EmployeeSalaryDetail.builder()
                .employeeSalary(employeeSalary)
                .salary(salary)
                .amount(amount)
                .componentType(SalaryComponentType.BASE_SALARY)
                .isProrated(true)
                .build();
    }

    private CalendarDate paidHoliday(LocalDate date) {
        CalendarDate calendarDate = new CalendarDate();
        calendarDate.setCode("CDD-" + date);
        calendarDate.setCalDate(date);
        calendarDate.setDayType(CalendarDayType.HOLIDAY);
        calendarDate.setIsPaidHoliday(true);
        calendarDate.setHolidayName("Holiday");
        return calendarDate;
    }

    private DailyWork dailyWork(LocalDate workingDate, Integer quantity, Integer otTime, LocalDateTime startTime,
            LocalDateTime endTime) {
        DailyWork dailyWork = new DailyWork();
        dailyWork.setCode("DWK-" + workingDate);
        dailyWork.setWorkingDate(workingDate);
        dailyWork.setQuantity(quantity == null ? 1 : quantity);
        dailyWork.setOtTime(otTime);
        dailyWork.setStartTime(startTime);
        dailyWork.setEndTime(endTime);
        dailyWork.setApprovalStatus(ApprovalStatus.APPROVED);
        return dailyWork;
    }

    private EmployeePto paidLeave(LocalDate startDate, LocalDate endDate) {
        EmployeePto pto = new EmployeePto();
        pto.setStartDate(startDate);
        pto.setEndDate(endDate);
        pto.setApprovalStatus(ApprovalStatus.APPROVED);
        pto.setPaidMode(LeavePaidMode.PAID);
        pto.setType("ANNUAL_LEAVE");
        return pto;
    }

    private EmployeePto unpaidLeave(LocalDate startDate, LocalDate endDate) {
        EmployeePto pto = new EmployeePto();
        pto.setStartDate(startDate);
        pto.setEndDate(endDate);
        pto.setApprovalStatus(ApprovalStatus.APPROVED);
        pto.setPaidMode(LeavePaidMode.UNPAID);
        pto.setType("UNPAID_LEAVE");
        return pto;
    }

    private PayrollAdjustment adjustment(PayrollAdjustmentType type, String amount, boolean isRetro, String reason) {
        PayrollAdjustment adjustment = new PayrollAdjustment();
        adjustment.setCode("PAD-" + type.name());
        adjustment.setAdjustmentType(type);
        adjustment.setAmount(amount);
        adjustment.setApprovalStatus(ApprovalStatus.APPROVED);
        adjustment.setIsRetro(isRetro);
        adjustment.setReason(reason);
        adjustment.setEffectiveDate(PERIOD_END);
        return adjustment;
    }

    private PayRateRule rateRule(PayrollRateRuleType rateType, PayRateDayType dayType, String multiplier) {
        PayRateRule rule = new PayRateRule();
        rule.setRateType(rateType);
        rule.setDayType(dayType);
        rule.setMultiplier(new BigDecimal(multiplier));
        rule.setAppliesTo(PayRateAppliesTo.BASE_ONLY);
        return rule;
    }

    private void assertLineAmount(List<PayrollLineDraft> lines, PayrollSourceType sourceType, String expectedAmount) {
        PayrollLineDraft line = lines.stream()
                .filter(candidate -> candidate.sourceType() == sourceType)
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal(expectedAmount), line.amount());
    }
}
