package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.services.CalendarDateService;

@Component
@Order(20)
public class ResolveExpectedWorkingHoursStep implements MonthlySalaryCalculationStep {

    private final CalendarDateService calendarDateService;

    public ResolveExpectedWorkingHoursStep(CalendarDateService calendarDateService) {
        this.calendarDateService = calendarDateService;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        Integer standardHoursPerDay = context.getPayrollPolicy().getStandardQuantityPerDay();
        BigDecimal expectedWorkingHourPerMonth = BigDecimal.ZERO;
        if (standardHoursPerDay != null && standardHoursPerDay > 0) {
            Map<DayType, Integer> dayTypeCounts = calendarDateService
                    .getCalendarDateTotalsByCompanyCodeAndMonth(context.getCompanyCode(), context.getMonth());
            int normalWorkingDays = dayTypeCounts.getOrDefault(DayType.NORMAL, 0);
            expectedWorkingHourPerMonth = BigDecimal.valueOf((long) standardHoursPerDay * normalWorkingDays);
        }

        if (expectedWorkingHourPerMonth.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_EXPECTED_WORKING_HOURS_INVALID);
        }
        context.setExpectedWorkingHourPerMonth(expectedWorkingHourPerMonth);
    }
}
