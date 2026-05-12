package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;

import com.dat.erp.constants.Messages;
import com.dat.erp.utils.CustomStringUtils;

public final class SalaryCalculationAmounts {

    private SalaryCalculationAmounts() {
    }

    public static BigDecimal toAmount(String value) {
        return CustomStringUtils.parsePositiveBigDecimal(value, Messages.ERROR_EMPLOYEE_SALARY_DETAIL_AMOUNT_INVALID);
    }
}
