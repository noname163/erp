package com.dat.erp.mapper.interfaces;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.dat.erp.constants.PayrollResultCalcBasis;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.dto.response.salary.MonthlySalaryDetailAuditResponse;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;

@Component
public class PayrollResultDetailCalculationMapper {

    public List<PayrollResultDetail> toDetails(
            PayrollResult payrollResult,
            MonthlySalaryCalculationResponse calculation) {
        if (calculation.getPayslip() != null) return List.of();
        List<PayrollResultDetail> details = new ArrayList<>();
        details.add(toSummaryDetail(payrollResult, calculation));
        if (calculation.getPaidLeaveHours() != null && calculation.getPaidLeaveHours().signum() > 0) {
            details.add(toAmountDetail(payrollResult,
                    PayrollResultCalcBasis.PAID_LEAVE,
                    calculation.getPaidLeaveHours(),
                    calculation.getStandardMoneyPerHour(),
                    BigDecimal.ZERO,
                    "Paid leave counted as paid working time"));
        }
        if (calculation.getUnpaidLeaveHours() != null && calculation.getUnpaidLeaveHours().signum() > 0) {
            details.add(toAmountDetail(payrollResult,
                    PayrollResultCalcBasis.UNPAID_LEAVE,
                    calculation.getUnpaidLeaveHours(),
                    calculation.getStandardMoneyPerHour(),
                    calculation.getUnpaidLeaveHours().multiply(calculation.getStandardMoneyPerHour()).negate(),
                    "Unpaid leave excluded from paid working time"));
        }
        if (calculation.getLateEarlyDeductionHours() != null && calculation.getLateEarlyDeductionHours().signum() > 0) {
            details.add(toAmountDetail(payrollResult,
                    PayrollResultCalcBasis.LATE_EARLY_DEDUCTION,
                    calculation.getLateEarlyDeductionHours(),
                    calculation.getStandardMoneyPerHour(),
                    calculation.getLateEarlyDeductionHours().multiply(calculation.getStandardMoneyPerHour()).negate(),
                    "Late arrival and early leave deducted from paid working time"));
        }
        if (calculation.getAuditTrail() != null) {
            for (MonthlySalaryDetailAuditResponse audit : calculation.getAuditTrail()) {
                details.add(toAuditDetail(payrollResult, audit));
            }
        }
        return details;
    }

    public PayrollResultDetail toSummaryDetail(
            PayrollResult payrollResult,
            MonthlySalaryCalculationResponse calculation) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setBasisHours(calculation.getExpectedWorkingHourPerMonth());
        detail.setPaidDays(calculation.getActualWorkingHourPerMonth());
        detail.setRatePerDay(calculation.getStandardMoneyPerHour());
        detail.setAmount(calculation.getFinalSalary());
        detail.setFormulaNote("basis=" + calculation.getSalaryBasisType() + ", expected="
                + calculation.getExpectedBasisValue() + ", actual=" + calculation.getActualBasisValue()
                + ", unit=" + calculation.getBasisUnit());
        return detail;
    }

    public PayrollResultDetail toAmountDetail(
            PayrollResult payrollResult,
            PayrollResultCalcBasis calcBasis,
            BigDecimal hours,
            BigDecimal rate,
            BigDecimal amount,
            String formulaNote) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setBasisHours(hours);
        detail.setRatePerDay(rate);
        detail.setAmount(amount);
        detail.setFormulaNote("type=" + calcBasis + ", " + formulaNote);
        return detail;
    }

    public PayrollResultDetail toAuditDetail(PayrollResult payrollResult, MonthlySalaryDetailAuditResponse audit) {
        PayrollResultDetail detail = new PayrollResultDetail();
        detail.setPayrollResult(payrollResult);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setBasisHours(audit.getBaseAmount());
        detail.setRatePerDay(audit.getConfiguredAmount());
        detail.setMultiplierApplied(audit.getDependencyAmount());
        detail.setAmount(audit.getResult());
        detail.setFormulaNote("salaryCode=" + audit.getSalaryCode() + ", method=" + audit.getCalculateMethod()
                + ", dependency=" + audit.getDependenceCode());
        return detail;
    }
}
