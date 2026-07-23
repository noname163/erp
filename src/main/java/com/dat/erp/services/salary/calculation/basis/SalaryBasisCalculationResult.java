package com.dat.erp.services.salary.calculation.basis;

import java.math.BigDecimal;
import java.util.Map;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.SalaryBasisType;

public record SalaryBasisCalculationResult(SalaryBasisType salaryBasisType, String basisUnit,
        BigDecimal expectedBasisValue, BigDecimal actualBasisValue, BigDecimal standardMoneyPerUnit,
        Map<DayType, BigDecimal> actualHoursByDayType, Map<String, BigDecimal> actualBasisValuesByType) {
}
