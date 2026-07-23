package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.salary.calculation.hour.ExpectedWorkingHourContext;
import com.dat.erp.services.salary.calculation.hour.ExpectedWorkingHourStrategyFactory;

@Component
@Order(20)
public class ResolveExpectedWorkingHoursStep implements MonthlySalaryCalculationStep {

    private final CalendarDateService calendarDateService;
    private final ExpectedWorkingHourStrategyFactory expectedWorkingHourStrategyFactory;

    public ResolveExpectedWorkingHoursStep(CalendarDateService calendarDateService,
            ExpectedWorkingHourStrategyFactory expectedWorkingHourStrategyFactory) {
        this.calendarDateService = calendarDateService;
        this.expectedWorkingHourStrategyFactory = expectedWorkingHourStrategyFactory;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        Map<DayType, Integer> dayTypeCounts = calendarDateService
                .getCalendarDateTotalsByCompanyCodeAndMonth(context.getCompanyCode(), context.getMonth());
        ExpectedWorkingHourContext hourContext = new ExpectedWorkingHourContext(context.getPayrollPolicy(),
                context.getMonth(), dayTypeCounts);
        BigDecimal expectedWorkingHourPerMonth = dayTypeCounts.keySet().stream()
                .map(dayType -> expectedWorkingHourStrategyFactory.calculate(dayType, hourContext))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (expectedWorkingHourPerMonth.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_EXPECTED_WORKING_HOURS_INVALID);
        }
        context.setExpectedWorkingHourPerMonth(expectedWorkingHourPerMonth);
    }
}
