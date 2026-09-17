package com.dat.erp.entities;

import java.util.List;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.data.MeasuredQuantityData;
import com.dat.erp.data.MonetaryAmountData;
import com.dat.erp.data.PayrollTraceData;
import com.dat.erp.utils.ErrorUtils;
import com.dat.erp.utils.UuidV7;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(exclude = { "payrollRun", "employeeSalary", "unit", "details" })
@Entity
@Table(name = "payroll_result", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_result_code_company_code", columnNames = { "code", "company_code" })
})
public class PayrollResult extends BaseAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_code", referencedColumnName = "code", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_salary_code", referencedColumnName = "code", nullable = false)
    private EmployeeSalary employeeSalary;

    @Column(name = "payslip_snapshot", columnDefinition = "text")
    private String payslipSnapshot;

    @Column(name = "calculation_error", length = 1000)
    private String calculationError;

    public void recordCalculationError(String error) { this.calculationError = error; }

    public void savePayslip(com.dat.erp.dto.response.MonthlyPayslipResponse payslip, String secret) {
        this.calculationError = null;
        if (payslip != null) this.payslipSnapshot = com.dat.erp.utils.CompanySecretKeyCryptoUtils.encrypt(
                com.dat.erp.utils.PayslipJson.write(payslip), secret);
    }

    @Column(name = "expected_amount")
    private String expectedAmount;

    @Column(name = "actual_amount")
    private String actualAmount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "expected_quantity")
    private Integer expectedQuantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_code", referencedColumnName = "code")
    private SystemUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private PayrollStatus sourceType;

    @Column(name = "is_retro")
    private boolean retro;

    @Column(name = "retro_reason")
    private String retroReason;

    @OneToMany(mappedBy = "payrollResult", fetch = FetchType.LAZY)
    private List<PayrollResultDetail> details;

    public PayrollResult(
            PayrollRun payrollRun,
            EmployeeSalary employeeSalary,
            MonetaryAmountData monetaryAmount,
            MeasuredQuantityData measuredQuantity,
            PayrollTraceData payrollTrace) {

        monetaryAmount = ErrorUtils.requireNonNull(monetaryAmount, "Payroll result amount data is required");
        measuredQuantity = ErrorUtils.requireNonNull(measuredQuantity, "Payroll result quantity data is required");
        payrollTrace = ErrorUtils.requireNonNull(payrollTrace, "Payroll result trace data is required");

        assignCode("PR" + UuidV7.generate());
        this.payrollRun = ErrorUtils.requireNonNull(payrollRun, "Payroll run is required");
        this.employeeSalary = ErrorUtils.requireNonNull(employeeSalary, "Employee salary is required");
        this.expectedAmount = ErrorUtils.requireNotBlank(
                monetaryAmount.getExpectedAmount(),
                "Expected amount is required");
        this.actualAmount = ErrorUtils.requireNotBlank(
                monetaryAmount.getActualAmount(),
                "Actual amount is required");
        this.currency = ErrorUtils.requireNotBlank(
                monetaryAmount.getCurrency(),
                "Currency is required");
        this.expectedQuantity = ErrorUtils.requireNonNegative(
                measuredQuantity.getExpectedQuantity(),
                "Expected quantity is required",
                "Expected quantity must not be negative");
        this.actualQuantity = ErrorUtils.requireNonNegative(
                measuredQuantity.getActualQuantity(),
                "Actual quantity is required",
                "Actual quantity must not be negative");
        this.unit = measuredQuantity.getSystemUnit();
        this.sourceType = ErrorUtils.requireNonNull(
                payrollTrace.getSourceType(),
                "Source type is required");
    }

    public static PayrollResult create(
            PayrollRun payrollRun,
            EmployeeSalary employeeSalary,
            MonetaryAmountData monetaryAmount,
            MeasuredQuantityData measuredQuantity,
            PayrollTraceData payrollTrace) {

        return new PayrollResult(payrollRun, employeeSalary, monetaryAmount, measuredQuantity, payrollTrace);
    }

    public void assignRetro(){
        this.retro = true;
    }

    public void assignRetroReason(String reason){
        this.retroReason = reason;
    }

    public void updateActualResult(
            String actualAmount,
            Integer actualQuantity) {

        this.actualAmount = ErrorUtils.requireNotBlank(
                actualAmount,
                "Actual amount is required");
        this.actualQuantity = ErrorUtils.requireNonNegative(
                actualQuantity,
                "Actual quantity is required",
                "Actual quantity must not be negative");
    }
}
