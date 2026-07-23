package com.dat.erp.services.salary.calculation.hour;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.DayType;
import com.dat.erp.entities.DailyWork;

@Component
public class ActualWorkingHourStrategyFactory {

    private final Map<DayType, ActualWorkingHourStrategy> strategiesByDayType = new EnumMap<>(DayType.class);
    private final ActualWorkingHourStrategy fallbackStrategy = new SummingActualWorkingHourStrategySupport() {
        @Override
        public DayType supports() {
            return null;
        }
    };

    public ActualWorkingHourStrategyFactory(List<ActualWorkingHourStrategy> strategies) {
        strategies.forEach(strategy -> strategiesByDayType.put(strategy.supports(), strategy));
    }

    public BigDecimal calculate(DayType dayType, List<DailyWork> worksForDayType) {
        return strategiesByDayType.getOrDefault(dayType, fallbackStrategy).calculate(worksForDayType);
    }
}
