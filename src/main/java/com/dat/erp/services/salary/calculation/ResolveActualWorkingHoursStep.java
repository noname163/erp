package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.constants.DayType;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;

@Component
@Order(30)
public class ResolveActualWorkingHoursStep implements MonthlySalaryCalculationStep {

    private final DailyWorkRepository dailyWorkRepository;

    public ResolveActualWorkingHoursStep(DailyWorkRepository dailyWorkRepository) {
        this.dailyWorkRepository = dailyWorkRepository;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        List<DailyWork> monthlyWorks = dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange(
                context.getEmployeeCode(), context.getCompanyCode(), context.getMonth().atDay(1),
                context.getMonth().atEndOfMonth());
        List<DailyWork> approvedWorks = monthlyWorks.stream()
                .filter(this::isApproved)
                .toList();

        Map<DayType, BigDecimal> actualHoursByDayType = new EnumMap<>(DayType.class);
        BigDecimal paidLeaveHours = BigDecimal.ZERO;
        BigDecimal unpaidLeaveHours = BigDecimal.ZERO;
        BigDecimal lateEarlyDeductionHours = BigDecimal.ZERO;
        BigDecimal overtimeHours = BigDecimal.ZERO;

        for (DailyWork dailyWork : approvedWorks) {
            DayType dayType = resolveDayType(dailyWork);
            BigDecimal hours = resolveHours(dailyWork);
            BigDecimal lateEarlyHours = minutesToHours(dailyWork.getLateMinutes())
                    .add(minutesToHours(dailyWork.getEarlyLeaveMinutes()));
            lateEarlyDeductionHours = lateEarlyDeductionHours.add(lateEarlyHours);
            overtimeHours = overtimeHours.add(dailyWork.getOvertimeHours() == null
                    ? minutesToHours(dailyWork.getOtTime())
                    : dailyWork.getOvertimeHours());

            if (dayType == DayType.PTO_PAID) {
                paidLeaveHours = paidLeaveHours.add(hours);
                actualHoursByDayType.merge(DayType.NORMAL, hours, BigDecimal::add);
                continue;
            }
            if (dayType == DayType.PTO_UNPAID || dayType == DayType.UNPAID_LEAVE) {
                unpaidLeaveHours = unpaidLeaveHours.add(hours);
                continue;
            }
            if (dayType == null || !isPaidWorkType(dayType)) {
                continue;
            }

            BigDecimal payableHours = hours.subtract(lateEarlyHours).max(BigDecimal.ZERO);
            actualHoursByDayType.merge(dayType, payableHours, BigDecimal::add);
        }

        context.setActualHoursByDayType(actualHoursByDayType);
        context.setActualWorkingHourPerMonth(
                actualHoursByDayType.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        context.setDailyWorks(approvedWorks);
        context.setPaidLeaveHours(paidLeaveHours);
        context.setUnpaidLeaveHours(unpaidLeaveHours);
        context.setLateEarlyDeductionHours(lateEarlyDeductionHours);
        context.setOvertimeHours(overtimeHours);
    }

    private boolean isApproved(DailyWork dailyWork) {
        return dailyWork.getApprovalStatus() == null || dailyWork.getApprovalStatus() == ApprovalStatus.APPROVED;
    }

    private DayType resolveDayType(DailyWork dailyWork) {
        return dailyWork.getWorkType() == null ? dailyWork.getDayType() : dailyWork.getWorkType();
    }

    private BigDecimal resolveHours(DailyWork dailyWork) {
        if (dailyWork.getHoursWorked() != null) {
            return dailyWork.getHoursWorked();
        }
        if (dailyWork.getQuantity() != null) {
            return BigDecimal.valueOf(dailyWork.getQuantity());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal minutesToHours(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 12, RoundingMode.HALF_UP);
    }

    private boolean isPaidWorkType(DayType dayType) {
        return dayType == DayType.NORMAL || dayType == DayType.HOLIDAY_WORK || dayType == DayType.WEEKEND_WORK;
    }
}
