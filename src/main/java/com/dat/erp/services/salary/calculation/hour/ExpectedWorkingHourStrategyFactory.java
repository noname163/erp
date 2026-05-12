package com.dat.erp.services.salary.calculation.hour;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;

@Component
public class ExpectedWorkingHourStrategyFactory {

    private final Map<DayType, ExpectedWorkingHourStrategy> strategiesByDayType = new EnumMap<>(DayType.class);

    public ExpectedWorkingHourStrategyFactory(List<ExpectedWorkingHourStrategy> strategies) {
        strategies.forEach(strategy -> strategiesByDayType.put(strategy.supports(), strategy));
    }

    public BigDecimal calculate(DayType dayType, ExpectedWorkingHourContext context) {
        ExpectedWorkingHourStrategy strategy = strategiesByDayType.get(dayType);
        if (strategy == null) {
            return BigDecimal.ZERO;
        }
        return strategy.calculate(context);
    }
}
