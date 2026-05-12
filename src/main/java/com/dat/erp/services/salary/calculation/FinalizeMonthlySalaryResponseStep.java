package com.dat.erp.services.salary.calculation;

import java.math.RoundingMode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(60)
public class FinalizeMonthlySalaryResponseStep implements MonthlySalaryCalculationStep {

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        context.setFinalSalary(context.getFinalSalary().setScale(4, RoundingMode.HALF_UP));
        context.getAuditTrail().forEach(item -> item.setTotalAmount(context.getFinalSalary()));
    }
}
