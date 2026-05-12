package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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
        Map<DayType, BigDecimal> actualHoursByDayType = new EnumMap<>(DayType.class);
        List<DailyWork> monthlyWorks = dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange(
                context.getEmployeeCode(), context.getCompanyCode(), context.getMonth().atDay(1),
                context.getMonth().atEndOfMonth());
        for (DailyWork dailyWork : monthlyWorks) {
            if (dailyWork.getWorkType() == null) {
                continue;
            }
            actualHoursByDayType.merge(dailyWork.getWorkType(),
                    dailyWork.getHoursWorked() == null ? BigDecimal.ZERO : dailyWork.getHoursWorked(), BigDecimal::add);
        }

        context.setActualHoursByDayType(actualHoursByDayType);
        context.setActualWorkingHourPerMonth(
                actualHoursByDayType.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
