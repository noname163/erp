package com.dat.erp.services.salary.calculation.basis;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.dat.erp.constants.DayType;
import com.dat.erp.entities.EmployeeKpiResult;
import com.dat.erp.entities.EmployeeProductionResult;
import com.dat.erp.services.salary.calculation.MonthlySalaryCalculationContext;

public record SalaryBasisCalculationContext(MonthlySalaryCalculationContext monthlyContext) {

    public BigDecimal expectedWorkingHourPerMonth() {
        return monthlyContext.getExpectedWorkingHourPerMonth();
    }

    public BigDecimal actualWorkingHourPerMonth() {
        return monthlyContext.getActualWorkingHourPerMonth();
    }

    public BigDecimal standardMoneyPerHour() {
        return monthlyContext.getStandardMoneyPerHour();
    }

    public Map<DayType, BigDecimal> actualHoursByDayType() {
        return monthlyContext.getActualHoursByDayType();
    }

    public List<EmployeeProductionResult> productionResults() {
        return monthlyContext.getProductionResults() == null ? List.of() : monthlyContext.getProductionResults();
    }

    public List<EmployeeKpiResult> kpiResults() {
        return monthlyContext.getKpiResults() == null ? List.of() : monthlyContext.getKpiResults();
    }
}
