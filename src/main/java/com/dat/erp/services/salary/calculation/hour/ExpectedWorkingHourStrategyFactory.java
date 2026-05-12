package com.dat.erp.services.salary.calculation.hour;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;

@Component
public class ExpectedWorkingHourStrategyFactory {

    private final Map<DayType, ExpectedWorkingHourStrategy> strategiesByDayType;

    public ExpectedWorkingHourStrategyFactory(List<ExpectedWorkingHourStrategy> strategies) {
        this.strategiesByDayType = new EnumMap<>(DayType.class);
        strategies.forEach(strategy -> strategiesByDayType.put(strategy.supports(), strategy));
    }

    public ExpectedWorkingHourStrategy getStrategy(DayType dayType) {
        return strategiesByDayType.getOrDefault(dayType, new NoOpExpectedWorkingHourStrategy(dayType));
    }

    private record NoOpExpectedWorkingHourStrategy(DayType supports) implements ExpectedWorkingHourStrategy {

        @Override
        public BigDecimal calculate(ExpectedWorkingHourContext context) {
            return BigDecimal.ZERO;
        }
    }
}
