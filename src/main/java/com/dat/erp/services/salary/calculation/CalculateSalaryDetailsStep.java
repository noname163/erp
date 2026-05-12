package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.services.salary.calculation.detail.SalaryDetailCalculationResult;
import com.dat.erp.services.salary.calculation.detail.SalaryDetailDependencyEvaluator;

@Component
@Order(50)
public class CalculateSalaryDetailsStep implements MonthlySalaryCalculationStep {

    private final SalaryDetailDependencyEvaluator salaryDetailDependencyEvaluator;

    public CalculateSalaryDetailsStep(SalaryDetailDependencyEvaluator salaryDetailDependencyEvaluator) {
        this.salaryDetailDependencyEvaluator = salaryDetailDependencyEvaluator;
    }

    @Override
    public void execute(MonthlySalaryCalculationContext context) {
        List<MonthlySalaryDetailAuditResponse> auditTrail = new ArrayList<>();
        BigDecimal finalSalary;
        if (context.getActualWorkingHourPerMonth().compareTo(context.getExpectedWorkingHourPerMonth()) == 0) {
            finalSalary = calculateWithFullAmountModel(context, auditTrail);
        } else {
            finalSalary = calculateWithHourlyModel(context, auditTrail);
        }

        context.setFinalSalary(finalSalary);
        context.setAuditTrail(auditTrail);
    }

    private BigDecimal calculateWithFullAmountModel(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        String salaryUnit = context.getEmployeeSalary().getCurrency();
        context.getDetails().forEach(detail -> {
            BigDecimal amount = SalaryCalculationAmounts.toAmount(detail.getAmount());
            auditTrail.add(new MonthlySalaryDetailAuditResponse(
                    detail.getSalary() == null ? null : detail.getSalary().getCode(),
                    detail.getDayType(),
                    detail.getSalary() == null ? null : detail.getSalary().getCalculateMethod(),
                    detail.getDependenceCode() == null ? null : detail.getDependenceCode().getCode(),
                    salaryUnit,
                    null,
                    amount,
                    null,
                    amount,
                    null));
        });

        return context.getDetails().stream()
                .map(detail -> SalaryCalculationAmounts.toAmount(detail.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateWithHourlyModel(MonthlySalaryCalculationContext context,
            List<MonthlySalaryDetailAuditResponse> auditTrail) {
        List<SalaryDetailCalculationResult> detailResults = salaryDetailDependencyEvaluator.evaluate(context);
        detailResults.forEach(result -> auditTrail.add(new MonthlySalaryDetailAuditResponse(
                result.salaryCode(),
                result.dayType(),
                result.calculateMethod(),
                result.dependenceCode(),
                context.getEmployeeSalary().getCurrency(),
                result.baseAmount(),
                result.configuredAmount(),
                result.dependencyAmount(),
                result.result(),
                null)));

        return detailResults.stream().map(SalaryDetailCalculationResult::result).reduce(BigDecimal.ZERO,
                BigDecimal::add);
    }
}
