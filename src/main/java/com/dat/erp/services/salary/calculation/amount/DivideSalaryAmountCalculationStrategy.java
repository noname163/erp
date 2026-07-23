package com.dat.erp.services.salary.calculation.amount;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.Messages;
import com.dat.erp.constants.SalaryCalculateMethod;
import com.dat.erp.exceptions.BadRequestException;

@Component
public class DivideSalaryAmountCalculationStrategy implements SalaryAmountCalculationStrategy {

    @Override
    public SalaryCalculateMethod supports() {
        return SalaryCalculateMethod.DIVIDE;
    }

    @Override
    public BigDecimal calculate(SalaryAmountCalculationContext context) {
        if (context.amountOrZero().compareTo(BigDecimal.ZERO) == 0) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_EXPECTED_WORKING_HOURS_INVALID);
        }
        return context.dependencyOrBase().divide(context.amountOrZero(), 12, RoundingMode.HALF_UP);
    }
}
