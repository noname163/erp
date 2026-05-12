package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.services.salary.calculation.hour.ActualWorkingHourStrategyFactory;

@Component
@Order(30)
public class ResolveActualWorkingHoursStep implements MonthlySalaryCalculationStep {

    private final DailyWorkRepository dailyWorkRepository;
    private final ActualWorkingHourStrategyFactory actualWorkingHourStrategyFactory;

    public ResolveActualWorkingHoursStep(DailyWorkRepository dailyWorkRepository,
            ActualWorkingHourStrategyFactory actualWorkingHourStrategyFactory) {
        this.dailyWorkRepository = dailyWorkRepository;
        this.actualWorkingHourStrategyFactory = actualWorkingHourStrategyFactory;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        List<DailyWork> monthlyWorks = dailyWorkRepository.findAllByEmployeeCodeAndCompanyCodeAndDateRange(
                context.getEmployeeCode(), context.getCompanyCode(), context.getMonth().atDay(1),
                context.getMonth().atEndOfMonth());
        Map<DayType, List<DailyWork>> worksByDayType = monthlyWorks.stream()
                .filter(dailyWork -> dailyWork.getWorkType() != null)
                .collect(Collectors.groupingBy(DailyWork::getWorkType, () -> new EnumMap<>(DayType.class),
                        Collectors.toList()));

        Map<DayType, BigDecimal> actualHoursByDayType = new EnumMap<>(DayType.class);
        worksByDayType.forEach((dayType, worksForDayType) -> actualHoursByDayType.put(dayType,
                actualWorkingHourStrategyFactory.calculate(dayType, worksForDayType)));

        context.setActualHoursByDayType(actualHoursByDayType);
        context.setActualWorkingHourPerMonth(
                actualHoursByDayType.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
