package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.constants.CalendarDayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayRateDayType;
import com.dat.erp.constants.PayrollAdjustmentType;
import com.dat.erp.constants.PayrollLineType;
import com.dat.erp.constants.PayrollProrationBasis;
import com.dat.erp.constants.PayrollRateRuleType;
import com.dat.erp.constants.PayrollResultCalcBasis;
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
import com.dat.erp.services.PayrollCalculationService;
import com.dat.erp.services.PayrollEmployeeDraft;
import com.dat.erp.services.PayrollLineDraft;

@Service
public class PayrollCalculationServiceImpl implements PayrollCalculationService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final String DEFAULT_CURRENCY = "VND";

    @Override
    public PayrollEmployeeDraft calculate(
            UserProfile userProfile,
            LocalDate periodStart,
            LocalDate periodEnd,
            List<EmploymentAgreement> agreements,
            List<EmployeeSalary> salaryRecords,
            List<EmployeeSalaryDetail> salaryDetails,
            List<PayrollPolicy> policies,
            List<PayRateRule> payRateRules,
            WorkSchedule workSchedule,
            List<WorkScheduleDetail> scheduleDetails,
            CompanyCalendar calendar,
            List<CalendarDate> calendarDates,
            List<DailyWork> dailyWorks,
            List<EmployeePto> ptoRecords,
            List<PayrollAdjustment> adjustments) {

        LocalDate effectiveStart = max(resolveEffectiveStart(userProfile, agreements, periodStart), periodStart);
        LocalDate effectiveEnd = min(resolveEffectiveEnd(userProfile, agreements, periodEnd), periodEnd);
        boolean retroOnly = adjustments.stream().anyMatch(adj -> Boolean.TRUE.equals(adj.getIsRetro()));

        if ((effectiveStart == null || effectiveEnd == null || effectiveStart.isAfter(effectiveEnd)) && !retroOnly) {
            return failure(userProfile, DEFAULT_CURRENCY, Messages.ERROR_PAYROLL_NO_SALARY_RECORD);
        }
        if (policies == null || policies.isEmpty()) {
            return failure(userProfile, DEFAULT_CURRENCY, Messages.ERROR_PAYROLL_POLICY_NOT_FOUND);
        }
        if (workSchedule == null || scheduleDetails == null || scheduleDetails.isEmpty()) {
            return failure(userProfile, DEFAULT_CURRENCY, Messages.ERROR_PAYROLL_MISSING_SCHEDULE);
        }
        if (calendar == null) {
            return failure(userProfile, DEFAULT_CURRENCY, Messages.ERROR_PAYROLL_MISSING_CALENDAR);
        }
        if ((salaryRecords == null || salaryRecords.isEmpty()) && !retroOnly) {
            return failure(userProfile, DEFAULT_CURRENCY, Messages.ERROR_PAYROLL_NO_SALARY_RECORD);
        }

        List<EmployeeSalary> sortedSalaryRecords = salaryRecords.stream()
                .sorted(Comparator.comparing(EmployeeSalary::getEffectiveFrom))
                .toList();
        if (hasOverlap(sortedSalaryRecords)) {
            return failure(userProfile, resolveCurrency(sortedSalaryRecords), Messages.ERROR_PAYROLL_OVERLAPPING_SALARY_RECORD);
        }
        if (hasGap(sortedSalaryRecords, effectiveStart, effectiveEnd)) {
            return failure(userProfile, resolveCurrency(sortedSalaryRecords), Messages.ERROR_PAYROLL_SALARY_GAP);
        }

        Map<String, List<EmployeeSalaryDetail>> detailsBySalaryCode = salaryDetails.stream()
                .filter(detail -> detail.getEmployeeSalary() != null && detail.getEmployeeSalary().getCode() != null)
                .collect(Collectors.groupingBy(detail -> detail.getEmployeeSalary().getCode()));

        return calculateWithResolvedInputs(userProfile, periodStart, periodEnd, effectiveStart, effectiveEnd, sortedSalaryRecords,
                detailsBySalaryCode, policies, payRateRules, scheduleDetails, calendarDates, dailyWorks, ptoRecords, adjustments);
    }

    private PayrollEmployeeDraft calculateWithResolvedInputs(
            UserProfile userProfile,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate effectiveStart,
            LocalDate effectiveEnd,
            List<EmployeeSalary> salaryRecords,
            Map<String, List<EmployeeSalaryDetail>> detailsBySalaryCode,
            List<PayrollPolicy> policies,
            List<PayRateRule> payRateRules,
            List<WorkScheduleDetail> scheduleDetails,
            List<CalendarDate> calendarDates,
            List<DailyWork> dailyWorks,
            List<EmployeePto> ptoRecords,
            List<PayrollAdjustment> adjustments) {
        PayrollPolicy policy = policies.stream().sorted(Comparator.comparing(PayrollPolicy::getEffectiveFrom)).findFirst()
                .orElse(null);
        if (policy == null) {
            return failure(userProfile, resolveCurrency(salaryRecords), Messages.ERROR_PAYROLL_POLICY_NOT_FOUND);
        }

        Map<LocalDate, CalendarDate> calendarByDate = calendarDates.stream()
                .collect(Collectors.toMap(CalendarDate::getCalDate, date -> date, (left, right) -> right));
        Map<Integer, WorkScheduleDetail> scheduleByDay = scheduleDetails.stream()
                .collect(Collectors.toMap(WorkScheduleDetail::getDayOfWeek, detail -> detail, (left, right) -> left));
        Map<LocalDate, List<DailyWork>> dailyWorkByDate = dailyWorks.stream()
                .collect(Collectors.groupingBy(DailyWork::getWorkingDate));
        Map<LocalDate, List<EmployeePto>> ptoByDate = explodePtoByDate(ptoRecords);
        BigDecimal periodUnits = countUnits(policy, scheduleByDay, calendarByDate, periodStart, periodEnd);
        if (periodUnits.compareTo(BigDecimal.ZERO) <= 0) {
            return failure(userProfile, resolveCurrency(salaryRecords), Messages.ERROR_PAYROLL_ZERO_DENOMINATOR);
        }

        List<PayrollLineDraft> lines = new ArrayList<>();
        for (EmployeeSalary salaryRecord : salaryRecords) {
            LocalDate segmentStart = max(effectiveStart, salaryRecord.getEffectiveFrom());
            LocalDate segmentEnd = min(effectiveEnd, salaryRecord.getEffectiveTo());
            if (segmentStart.isAfter(segmentEnd)) {
                continue;
            }

            List<EmployeeSalaryDetail> recordDetails = detailsBySalaryCode.getOrDefault(salaryRecord.getCode(), List.of());
            if (recordDetails.isEmpty()) {
                return failure(userProfile, resolveCurrency(salaryRecords), Messages.ERROR_PAYROLL_NO_SALARY_RECORD);
            }

            BigDecimal segmentUnits = countUnits(policy, scheduleByDay, calendarByDate, segmentStart, segmentEnd);
            EmployeeSalaryDetail baseDetail = resolveBaseDetail(recordDetails);
            BigDecimal baseMonthlyAmount = baseDetail == null ? BigDecimal.ZERO : parseAmount(baseDetail.getAmount());
            BigDecimal baseDayRate = divide(baseMonthlyAmount, periodUnits);
            BigDecimal baseHourRate = divide(baseDayRate, BigDecimal.valueOf(policy.getStandardHoursPerDay() == null ? 8
                    : policy.getStandardHoursPerDay()));

            for (EmployeeSalaryDetail detail : recordDetails) {
                if (detail.getSalary() == null) {
                    continue;
                }
                BigDecimal monthlyAmount = parseAmount(detail.getAmount());
                boolean deductionComponent = Boolean.TRUE.equals(detail.getSalary().getIsDeduct())
                        || detail.getComponentType() == SalaryComponentType.DEDUCTION
                        || detail.getSalary().getComponentType() == SalaryComponentType.DEDUCTION;
                BigDecimal proratedAmount = divide(monthlyAmount.multiply(segmentUnits), periodUnits);
                lines.add(new PayrollLineDraft(
                        detail.getSalary(),
                        deductionComponent ? PayrollLineType.DEDUCTION : PayrollLineType.EARNING,
                        deductionComponent ? PayrollSourceType.DEDUCTION : PayrollSourceType.SALARY,
                        proratedAmount,
                        segmentUnits,
                        resolveCurrency(salaryRecords),
                        monthlyAmount,
                        BigDecimal.ONE,
                        segmentStart,
                        segmentEnd,
                        false,
                        false,
                        null,
                        salaryRecord.getCode(),
                        "Segment monthly amount",
                        basisFor(policy.getProrationBasis()),
                        null,
                        null));

                if (shouldDeductForUnpaidLeave(detail, policy)) {
                    BigDecimal unpaidLeaveUnits = countUnpaidLeaveUnits(segmentStart, segmentEnd, ptoByDate, calendarByDate,
                            policy);
                    if (unpaidLeaveUnits.compareTo(BigDecimal.ZERO) > 0) {
                        lines.add(new PayrollLineDraft(
                                detail.getSalary(),
                                PayrollLineType.DEDUCTION,
                                PayrollSourceType.PTO,
                                divide(monthlyAmount.multiply(unpaidLeaveUnits), periodUnits),
                                unpaidLeaveUnits,
                                resolveCurrency(salaryRecords),
                                divide(monthlyAmount, periodUnits),
                                BigDecimal.ONE,
                                segmentStart,
                                segmentEnd,
                                false,
                                false,
                                null,
                                salaryRecord.getCode(),
                                "Unpaid leave deduction",
                                basisFor(policy.getProrationBasis()),
                                null,
                                null));
                    }
                }
            }

            addPremiumLines(lines, recordDetails, resolveCurrency(salaryRecords), payRateRules, policy, segmentStart, segmentEnd,
                    dailyWorkByDate, calendarByDate, baseDayRate, baseHourRate);
        }

        addAdjustmentLines(lines, adjustments, resolveCurrency(salaryRecords), periodStart, periodEnd);
        BigDecimal grossAmount = lines.stream()
                .filter(line -> line.lineType() != PayrollLineType.DEDUCTION)
                .map(PayrollLineDraft::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deductionAmount = lines.stream()
                .filter(line -> line.lineType() == PayrollLineType.DEDUCTION)
                .map(PayrollLineDraft::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PayrollEmployeeDraft(
                userProfile,
                resolveCurrency(salaryRecords),
                grossAmount.setScale(2, RoundingMode.HALF_UP),
                deductionAmount.setScale(2, RoundingMode.HALF_UP),
                grossAmount.subtract(deductionAmount).setScale(2, RoundingMode.HALF_UP),
                PayrollSummaryStatus.SUCCESS,
                false,
                null,
                lines);
    }

    private LocalDate resolveEffectiveStart(UserProfile userProfile, List<EmploymentAgreement> agreements, LocalDate fallback) {
        if (agreements != null && !agreements.isEmpty()) {
            return agreements.stream()
                    .map(EmploymentAgreement::getEffectiveFrom)
                    .filter(Objects::nonNull)
                    .min(LocalDate::compareTo)
                    .orElse(fallback);
        }
        return userProfile.getHireDate() == null ? fallback : max(userProfile.getHireDate(), fallback);
    }

    private LocalDate resolveEffectiveEnd(UserProfile userProfile, List<EmploymentAgreement> agreements, LocalDate fallback) {
        if (agreements != null && !agreements.isEmpty()) {
            return agreements.stream()
                    .map(agreement -> agreement.getResignationDate() != null ? agreement.getResignationDate()
                            : agreement.getTerminationDate() != null ? agreement.getTerminationDate() : agreement.getEffectiveTo())
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(fallback);
        }
        return Boolean.FALSE.equals(userProfile.getIsActive()) ? null : fallback;
    }

    private boolean hasOverlap(List<EmployeeSalary> salaryRecords) {
        for (int index = 1; index < salaryRecords.size(); index++) {
            if (!salaryRecords.get(index).getEffectiveFrom().isAfter(salaryRecords.get(index - 1).getEffectiveTo())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasGap(List<EmployeeSalary> salaryRecords, LocalDate effectiveStart, LocalDate effectiveEnd) {
        if (salaryRecords.isEmpty()) {
            return true;
        }
        if (salaryRecords.get(0).getEffectiveFrom().isAfter(effectiveStart)
                || salaryRecords.get(salaryRecords.size() - 1).getEffectiveTo().isBefore(effectiveEnd)) {
            return true;
        }
        for (int index = 1; index < salaryRecords.size(); index++) {
            LocalDate expected = salaryRecords.get(index - 1).getEffectiveTo().plusDays(1);
            if (!salaryRecords.get(index).getEffectiveFrom().isEqual(expected)) {
                return true;
            }
        }
        return false;
    }

    private Map<LocalDate, List<EmployeePto>> explodePtoByDate(List<EmployeePto> ptoRecords) {
        Map<LocalDate, List<EmployeePto>> result = new HashMap<>();
        for (EmployeePto pto : ptoRecords) {
            if (!isApproved(pto.getApprovalStatus()) || pto.getStartDate() == null || pto.getEndDate() == null) {
                continue;
            }
            LocalDate cursor = pto.getStartDate();
            while (!cursor.isAfter(pto.getEndDate())) {
                result.computeIfAbsent(cursor, ignored -> new ArrayList<>()).add(pto);
                cursor = cursor.plusDays(1);
            }
        }
        return result;
    }

    private BigDecimal countUnits(PayrollPolicy policy, Map<Integer, WorkScheduleDetail> scheduleByDay,
            Map<LocalDate, CalendarDate> calendarByDate, LocalDate startDate, LocalDate endDate) {
        PayrollProrationBasis basis = policy.getProrationBasis() == null ? PayrollProrationBasis.CALENDAR_DAYS
                : policy.getProrationBasis();
        if (basis == PayrollProrationBasis.FULL_MONTH) {
            basis = PayrollProrationBasis.CALENDAR_DAYS;
        }
        BigDecimal units = BigDecimal.ZERO;
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            ResolvedDayType dayType = classifyDay(cursor, calendarByDate.get(cursor));
            WorkScheduleDetail detail = scheduleByDay.get(cursor.getDayOfWeek().getValue());
            boolean workingDay = detail != null && Boolean.TRUE.equals(detail.getIsWorkingDay());
            boolean payableHoliday = isPaidHoliday(calendarByDate.get(cursor), policy) && workingDay;
            switch (basis) {
                case CALENDAR_DAYS -> units = units.add(BigDecimal.ONE);
                case WORKING_DAYS, ATTENDANCE_QUANTITY -> {
                    if ((workingDay && dayType == ResolvedDayType.WORKDAY) || payableHoliday) {
                        units = units.add(BigDecimal.ONE);
                    }
                }
                case HOURS -> units = units.add(BigDecimal.valueOf(resolveScheduleHours(detail, policy)));
                case MINUTES -> units = units.add(BigDecimal.valueOf(resolveScheduleMinutes(detail, policy)));
                default -> units = units.add(BigDecimal.ONE);
            }
            cursor = cursor.plusDays(1);
        }
        return units.setScale(2, RoundingMode.HALF_UP);
    }

    private EmployeeSalaryDetail resolveBaseDetail(List<EmployeeSalaryDetail> recordDetails) {
        return recordDetails.stream()
                .filter(detail -> detail.getSalary() != null)
                .filter(detail -> detail.getComponentType() == SalaryComponentType.BASE_SALARY
                        || detail.getSalary().getComponentType() == SalaryComponentType.BASE_SALARY)
                .findFirst()
                .orElseGet(() -> recordDetails.stream()
                        .filter(detail -> detail.getSalary() != null && !Boolean.TRUE.equals(detail.getSalary().getIsDeduct()))
                        .findFirst()
                        .orElse(null));
    }

    private boolean shouldDeductForUnpaidLeave(EmployeeSalaryDetail detail, PayrollPolicy policy) {
        PayrollProrationBasis basis = detail.getProrationBasisOverride() == null ? detail.getSalary().getDefaultProrationBasis()
                : detail.getProrationBasisOverride();
        if (basis == null) {
            basis = policy.getProrationBasis();
        }
        return !Boolean.TRUE.equals(detail.getSalary().getIsDeduct())
                && (detail.getIsProrated() == null ? basis != PayrollProrationBasis.FULL_MONTH : detail.getIsProrated());
    }

    private BigDecimal countUnpaidLeaveUnits(LocalDate startDate, LocalDate endDate, Map<LocalDate, List<EmployeePto>> ptoByDate,
            Map<LocalDate, CalendarDate> calendarByDate, PayrollPolicy policy) {
        BigDecimal result = BigDecimal.ZERO;
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            if (!isPaidHoliday(calendarByDate.get(cursor), policy)) {
                boolean unpaid = ptoByDate.getOrDefault(cursor, List.of()).stream().anyMatch(this::isUnpaidLeave);
                if (unpaid) {
                    result = result.add(BigDecimal.ONE);
                }
            }
            cursor = cursor.plusDays(1);
        }
        return result;
    }

    private void addPremiumLines(List<PayrollLineDraft> lines, List<EmployeeSalaryDetail> recordDetails, String currency,
            List<PayRateRule> payRateRules, PayrollPolicy policy, LocalDate segmentStart, LocalDate segmentEnd,
            Map<LocalDate, List<DailyWork>> dailyWorkByDate, Map<LocalDate, CalendarDate> calendarByDate, BigDecimal baseDayRate,
            BigDecimal baseHourRate) {
        LocalDate cursor = segmentStart;
        while (!cursor.isAfter(segmentEnd)) {
            ResolvedDayType dayType = classifyDay(cursor, calendarByDate.get(cursor));
            for (DailyWork dailyWork : dailyWorkByDate.getOrDefault(cursor, List.of())) {
                if (!isApproved(dailyWork.getApprovalStatus())) {
                    continue;
                }
                if (dayType == ResolvedDayType.HOLIDAY) {
                    addDayPremium(lines, recordDetails, currency, dailyWork, baseDayRate,
                            resolveMultiplier(payRateRules, PayrollRateRuleType.DAY_PREMIUM, PayRateDayType.HOLIDAY),
                            PayrollSourceType.HOLIDAY_PREMIUM, "Holiday premium");
                } else if (dayType == ResolvedDayType.WEEKEND) {
                    addDayPremium(lines, recordDetails, currency, dailyWork, baseDayRate,
                            resolveMultiplier(payRateRules, PayrollRateRuleType.DAY_PREMIUM, PayRateDayType.WEEKEND),
                            PayrollSourceType.WEEKEND_PREMIUM, "Weekend premium");
                }
                BigDecimal otHours = dailyWork.getOtTime() == null ? BigDecimal.ZERO : BigDecimal.valueOf(dailyWork.getOtTime());
                if (otHours.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal multiplier = resolveMultiplier(payRateRules, PayrollRateRuleType.OVERTIME, mapDayType(dayType));
                    lines.add(new PayrollLineDraft(resolveBaseSalary(recordDetails), PayrollLineType.PREMIUM, PayrollSourceType.OT,
                            divide(baseHourRate.multiply(multiplier).multiply(otHours), BigDecimal.ONE), otHours, currency,
                            baseHourRate, multiplier, cursor, cursor, false, false, null, dailyWork.getCode(),
                            "Overtime premium", PayrollResultCalcBasis.HOURS, null, cursor));
                }
                BigDecimal nightHours = calculateNightHours(dailyWork, policy);
                if (nightHours.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal multiplier = resolveMultiplier(payRateRules, PayrollRateRuleType.NIGHT_PREMIUM,
                            mapDayType(dayType));
                    lines.add(new PayrollLineDraft(resolveBaseSalary(recordDetails), PayrollLineType.PREMIUM,
                            PayrollSourceType.NIGHT_PREMIUM,
                            divide(baseHourRate.multiply(multiplier).multiply(nightHours), BigDecimal.ONE), nightHours, currency,
                            baseHourRate, multiplier, cursor, cursor, false, false, null, dailyWork.getCode(),
                            "Night premium", PayrollResultCalcBasis.HOURS, null, cursor));
                }
            }
            cursor = cursor.plusDays(1);
        }
    }

    private void addDayPremium(List<PayrollLineDraft> lines, List<EmployeeSalaryDetail> recordDetails, String currency,
            DailyWork dailyWork, BigDecimal baseDayRate, BigDecimal multiplier, PayrollSourceType sourceType, String formulaNote) {
        BigDecimal quantity = BigDecimal.valueOf(dailyWork.getQuantity() == null ? 1 : dailyWork.getQuantity());
        lines.add(new PayrollLineDraft(resolveBaseSalary(recordDetails), PayrollLineType.PREMIUM, sourceType,
                divide(baseDayRate.multiply(multiplier).multiply(quantity), BigDecimal.ONE), quantity, currency, baseDayRate,
                multiplier, dailyWork.getWorkingDate(), dailyWork.getWorkingDate(), false, false, null, dailyWork.getCode(),
                formulaNote, PayrollResultCalcBasis.WORKING_DAYS, null, dailyWork.getWorkingDate()));
    }

    private PayrollEmployeeDraft failure(UserProfile userProfile, String currency, String message) {
        return new PayrollEmployeeDraft(userProfile, currency, ZERO, ZERO, ZERO, PayrollSummaryStatus.FAILED, true, message,
                List.of());
    }

    private void addAdjustmentLines(List<PayrollLineDraft> lines, List<PayrollAdjustment> adjustments, String currency,
            LocalDate periodStart, LocalDate periodEnd) {
        for (PayrollAdjustment adjustment : adjustments) {
            if (!isApproved(adjustment.getApprovalStatus())) {
                continue;
            }
            BigDecimal amount = parseAmount(adjustment.getAmount());
            PayrollLineType lineType = switch (adjustment.getAdjustmentType()) {
                case DEDUCTION -> PayrollLineType.DEDUCTION;
                case FINAL_SETTLEMENT -> PayrollLineType.SETTLEMENT;
                case RETRO -> PayrollLineType.RETRO;
                default -> PayrollLineType.EARNING;
            };
            PayrollSourceType sourceType = switch (adjustment.getAdjustmentType()) {
                case BONUS -> PayrollSourceType.BONUS;
                case DEDUCTION -> PayrollSourceType.DEDUCTION;
                case RETRO -> PayrollSourceType.RETRO;
                case FINAL_SETTLEMENT -> PayrollSourceType.FINAL_SETTLEMENT;
            };
            lines.add(new PayrollLineDraft(
                    adjustment.getSalary(),
                    lineType,
                    sourceType,
                    amount,
                    adjustment.getQuantity(),
                    currency,
                    amount,
                    BigDecimal.ONE,
                    periodStart,
                    periodEnd,
                    adjustment.getAdjustmentType() == PayrollAdjustmentType.RETRO || Boolean.TRUE.equals(adjustment.getIsRetro()),
                    true,
                    adjustment.getReason(),
                    adjustment.getCode(),
                    adjustment.getReason(),
                    PayrollResultCalcBasis.FORMULA,
                    null,
                    adjustment.getEffectiveDate()));
        }
    }

    private Salary resolveBaseSalary(List<EmployeeSalaryDetail> recordDetails) {
        EmployeeSalaryDetail baseDetail = resolveBaseDetail(recordDetails);
        return baseDetail == null ? null : baseDetail.getSalary();
    }

    private BigDecimal resolveMultiplier(List<PayRateRule> payRateRules, PayrollRateRuleType ruleType, PayRateDayType dayType) {
        return payRateRules.stream()
                .filter(rule -> rule.getRateType() == ruleType)
                .filter(rule -> rule.getDayType() == null || rule.getDayType() == dayType)
                .map(PayRateRule::getMultiplier)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> switch (ruleType) {
                    case DAY_PREMIUM -> dayType == PayRateDayType.HOLIDAY ? new BigDecimal("3.0")
                            : dayType == PayRateDayType.WEEKEND ? new BigDecimal("2.0") : BigDecimal.ONE;
                    case OVERTIME -> new BigDecimal("1.5");
                    case NIGHT_PREMIUM -> new BigDecimal("0.25");
                });
    }

    private BigDecimal calculateNightHours(DailyWork dailyWork, PayrollPolicy policy) {
        if (dailyWork.getStartTime() == null || dailyWork.getEndTime() == null) {
            return BigDecimal.ZERO;
        }
        LocalTime nightStart = policy.getNightPremiumStart() == null ? LocalTime.of(19, 0) : policy.getNightPremiumStart();
        LocalTime nightEnd = policy.getNightPremiumEnd() == null ? LocalTime.of(23, 59, 59) : policy.getNightPremiumEnd();
        LocalDateTime overlapStart = dailyWork.getStartTime().isAfter(LocalDateTime.of(dailyWork.getWorkingDate(), nightStart))
                ? dailyWork.getStartTime()
                : LocalDateTime.of(dailyWork.getWorkingDate(), nightStart);
        LocalDateTime overlapEnd = dailyWork.getEndTime().isBefore(LocalDateTime.of(dailyWork.getWorkingDate(), nightEnd))
                ? dailyWork.getEndTime()
                : LocalDateTime.of(dailyWork.getWorkingDate(), nightEnd);
        if (!overlapEnd.isAfter(overlapStart)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(Duration.between(overlapStart, overlapEnd).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private ResolvedDayType classifyDay(LocalDate date, CalendarDate calendarDate) {
        if (calendarDate != null) {
            CalendarDayType type = calendarDate.getDayType();
            if (type == CalendarDayType.HOLIDAY || type == CalendarDayType.COMPANY_HOLIDAY
                    || type == CalendarDayType.SUBSTITUTE_HOLIDAY || type == CalendarDayType.SPECIAL_EVENT_PAID_DAY) {
                return ResolvedDayType.HOLIDAY;
            }
            if (type == CalendarDayType.SHUTDOWN || type == CalendarDayType.EMERGENCY_CLOSURE) {
                return ResolvedDayType.DAY_OFF;
            }
            if (type == CalendarDayType.MAKE_UP_WORKDAY || type == CalendarDayType.WORKDAY) {
                return ResolvedDayType.WORKDAY;
            }
            if (type == CalendarDayType.WEEKEND) {
                return ResolvedDayType.WEEKEND;
            }
        }
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> ResolvedDayType.WEEKEND;
            default -> ResolvedDayType.WORKDAY;
        };
    }

    private boolean isPaidHoliday(CalendarDate calendarDate, PayrollPolicy policy) {
        if (calendarDate == null) {
            return false;
        }
        boolean holiday = classifyDay(calendarDate.getCalDate(), calendarDate) == ResolvedDayType.HOLIDAY;
        return holiday && Boolean.TRUE.equals(policy.getPayHolidayIfOff());
    }

    private boolean isUnpaidLeave(EmployeePto pto) {
        return pto.getPaidMode() == com.dat.erp.constants.LeavePaidMode.UNPAID
                || "UNPAID".equalsIgnoreCase(pto.getStatus())
                || "UNPAID_LEAVE".equalsIgnoreCase(pto.getType())
                || "UNPAID_LEAVE".equalsIgnoreCase(pto.getLeaveType());
    }

    private boolean isApproved(ApprovalStatus approvalStatus) {
        return approvalStatus == null || approvalStatus == ApprovalStatus.APPROVED;
    }

    private int resolveScheduleHours(WorkScheduleDetail detail, PayrollPolicy policy) {
        if (detail != null && detail.getStartTime() != null && detail.getEndTime() != null) {
            long minutes = Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes();
            int breakMinutes = detail.getBreakMinutes() == null ? 0 : detail.getBreakMinutes();
            return (int) Math.max(0, (minutes - breakMinutes) / 60);
        }
        return policy.getStandardHoursPerDay() == null ? 8 : policy.getStandardHoursPerDay();
    }

    private int resolveScheduleMinutes(WorkScheduleDetail detail, PayrollPolicy policy) {
        if (detail != null && detail.getStartTime() != null && detail.getEndTime() != null) {
            long minutes = Duration.between(detail.getStartTime(), detail.getEndTime()).toMinutes();
            int breakMinutes = detail.getBreakMinutes() == null ? 0 : detail.getBreakMinutes();
            return (int) Math.max(0, minutes - breakMinutes);
        }
        if (policy.getStandardMinutesPerDay() != null) {
            return policy.getStandardMinutesPerDay();
        }
        return (policy.getStandardHoursPerDay() == null ? 8 : policy.getStandardHoursPerDay()) * 60;
    }

    private PayRateDayType mapDayType(ResolvedDayType dayType) {
        return switch (dayType) {
            case HOLIDAY -> PayRateDayType.HOLIDAY;
            case WEEKEND -> PayRateDayType.WEEKEND;
            case DAY_OFF -> PayRateDayType.DAY_OFF;
            default -> PayRateDayType.NORMAL;
        };
    }

    private PayrollResultCalcBasis basisFor(PayrollProrationBasis basis) {
        if (basis == null) {
            return PayrollResultCalcBasis.WORKING_DAYS;
        }
        return switch (basis) {
            case CALENDAR_DAYS -> PayrollResultCalcBasis.CALENDAR_DAYS;
            case HOURS -> PayrollResultCalcBasis.HOURS;
            case MINUTES -> PayrollResultCalcBasis.MINUTES;
            case ATTENDANCE_QUANTITY -> PayrollResultCalcBasis.QUANTITY;
            case FULL_MONTH -> PayrollResultCalcBasis.FULL_MONTH;
            default -> PayrollResultCalcBasis.WORKING_DAYS;
        };
    }

    private BigDecimal divide(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal parseAmount(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(rawValue.trim()).setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveCurrency(List<EmployeeSalary> salaryRecords) {
        return salaryRecords.stream()
                .map(EmployeeSalary::getCurrency)
                .filter(Objects::nonNull)
                .filter(currency -> !currency.isBlank())
                .map(currency -> currency.trim().toUpperCase())
                .findFirst()
                .orElse(DEFAULT_CURRENCY);
    }

    private LocalDate max(LocalDate left, LocalDate right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isAfter(right) ? left : right;
    }

    private LocalDate min(LocalDate left, LocalDate right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.isBefore(right) ? left : right;
    }

    private enum ResolvedDayType {
        WORKDAY,
        WEEKEND,
        HOLIDAY,
        DAY_OFF
    }
}
