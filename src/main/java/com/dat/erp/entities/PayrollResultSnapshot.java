package com.dat.erp.entities;

import java.time.LocalDateTime;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.data.EmployeeOwnedRecordData;
import com.dat.erp.data.MeasuredQuantityData;
import com.dat.erp.data.MonetaryAmountData;
import com.dat.erp.data.PayrollTraceData;
import com.dat.erp.data.PayrollResultSnapshotData;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.utils.ErrorUtils;
import com.dat.erp.utils.UuidV7;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payroll_result_snapshot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayrollResultSnapshot extends BaseAuditableEntity {

    @Column(name = "payroll_run_code")
    private String payrollRunCode;

    @Column(name = "payroll_result_code")
    private String payrollResultCode;

    @Column(name = "rerun_batch_code")
    private String rerunBatchCode;

    @Column(name = "employee_salary_code")
    private String employeeSalaryCode;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "expected_amount")
    private String expectedAmount;

    @Column(name = "actual_amount")
    private String actualAmount;

    @Column(name = "expected_quantity")
    private Integer expectedQuantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @Column(name = "currency")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private PayrollStatus sourceType;

    @Lob
    @Column(name = "result_json")
    private String resultJson;

    @Lob
    @Column(name = "detail_json")
    private String detailJson;

    @Column(name = "snapshot_at")
    private LocalDateTime snapshotAt;

    @Column(name = "snapshot_by")
    private String snapshotBy;

    private PayrollResultSnapshot(PayrollResultSnapshotData data) {
        if (data == null) {
            throw new BadRequestException(
                    "Payroll result snapshot data is required");
        }
        assignCode("PRS" + UuidV7.generate());
        this.payrollRunCode = ErrorUtils.requireNotBlank(
                data.getPayrollRunCode(),
                "Payroll run code is required");

        this.payrollResultCode = ErrorUtils.requireNotBlank(
                data.getPayrollResultCode(),
                "Payroll result code is required");

        this.rerunBatchCode = ErrorUtils.requireNotBlank(
                data.getRerunBatchCode(),
                "Rerun batch code is required");

        this.employeeSalaryCode = ErrorUtils.requireNotBlank(
                data.getEmployeeSalaryCode(),
                "Employee salary code is required");

        this.employeeCode = ErrorUtils.requireNotBlank(
                data.getEmployeeCode(),
                "Employee code is required");

        this.expectedAmount = ErrorUtils.requireNotBlank(
                data.getExpectedAmount(),
                "Expected amount is required");

        this.actualAmount = ErrorUtils.requireNotBlank(
                data.getActualAmount(),
                "Actual amount is required");

        this.expectedQuantity = ErrorUtils.requireNonNegative(
                data.getExpectedQuantity(),
                "Expected quantity is required",
                "Expected quantity must not be negative");

        this.actualQuantity = ErrorUtils.requireNonNegative(
                data.getActualQuantity(),
                "Actual quantity is required",
                "Actual quantity must not be negative");

        this.currency = ErrorUtils.requireNotBlank(
                data.getCurrency(),
                "Currency is required");

        this.sourceType = ErrorUtils.requireNonNull(
                data.getSourceType(),
                "Source type is required");

        this.resultJson = ErrorUtils.requireNotBlank(
                data.getResultJson(),
                "Result JSON is required");

        this.detailJson = ErrorUtils.requireNotBlank(
                data.getDetailJson(),
                "Detail JSON is required");

        this.snapshotAt = ErrorUtils.requireNonNull(
                data.getSnapshotAt(),
                "Snapshot time is required");

        this.snapshotBy = ErrorUtils.requireNotBlank(
                data.getSnapshotBy(),
                "Snapshot user is required");
    }

    private PayrollResultSnapshot(
            PayrollTraceData payrollTrace,
            EmployeeOwnedRecordData employeeOwner,
            String employeeSalaryCode,
            MonetaryAmountData monetaryAmount,
            MeasuredQuantityData measuredQuantity,
            String resultJson,
            String detailJson,
            LocalDateTime snapshotAt,
            String snapshotBy) {

        payrollTrace = ErrorUtils.requireNonNull(payrollTrace, "Payroll snapshot trace data is required");
        employeeOwner = ErrorUtils.requireNonNull(employeeOwner, "Payroll snapshot employee owner data is required");
        monetaryAmount = ErrorUtils.requireNonNull(monetaryAmount, "Payroll snapshot amount data is required");
        measuredQuantity = ErrorUtils.requireNonNull(measuredQuantity, "Payroll snapshot quantity data is required");

        assignCode("PRS" + UuidV7.generate());
        this.payrollRunCode = ErrorUtils.requireNotBlank(
                payrollTrace.getPayrollRunCode(),
                "Payroll run code is required");
        this.payrollResultCode = ErrorUtils.requireNotBlank(
                payrollTrace.getPayrollResultCode(),
                "Payroll result code is required");
        this.rerunBatchCode = ErrorUtils.requireNotBlank(
                payrollTrace.getRerunBatchCode(),
                "Rerun batch code is required");
        this.employeeSalaryCode = ErrorUtils.requireNotBlank(
                employeeSalaryCode,
                "Employee salary code is required");
        this.employeeCode = ErrorUtils.requireNotBlank(
                employeeOwner.getEmployeeCode(),
                "Employee code is required");
        this.expectedAmount = ErrorUtils.requireNotBlank(
                monetaryAmount.getExpectedAmount(),
                "Expected amount is required");
        this.actualAmount = ErrorUtils.requireNotBlank(
                monetaryAmount.getActualAmount(),
                "Actual amount is required");
        this.expectedQuantity = ErrorUtils.requireNonNegative(
                measuredQuantity.getExpectedQuantity(),
                "Expected quantity is required",
                "Expected quantity must not be negative");
        this.actualQuantity = ErrorUtils.requireNonNegative(
                measuredQuantity.getActualQuantity(),
                "Actual quantity is required",
                "Actual quantity must not be negative");
        this.currency = ErrorUtils.requireNotBlank(
                monetaryAmount.getCurrency(),
                "Currency is required");
        this.sourceType = ErrorUtils.requireNonNull(
                payrollTrace.getSourceType(),
                "Source type is required");
        this.resultJson = ErrorUtils.requireNotBlank(
                resultJson,
                "Result JSON is required");
        this.detailJson = ErrorUtils.requireNotBlank(
                detailJson,
                "Detail JSON is required");
        this.snapshotAt = ErrorUtils.requireNonNull(
                snapshotAt,
                "Snapshot time is required");
        this.snapshotBy = ErrorUtils.requireNotBlank(
                snapshotBy,
                "Snapshot user is required");
    }

    public static PayrollResultSnapshot create(
            PayrollResultSnapshotData data) {
        return new PayrollResultSnapshot(data);
    }

    public static PayrollResultSnapshot create(
            PayrollTraceData payrollTrace,
            EmployeeOwnedRecordData employeeOwner,
            String employeeSalaryCode,
            MonetaryAmountData monetaryAmount,
            MeasuredQuantityData measuredQuantity,
            String resultJson,
            String detailJson,
            LocalDateTime snapshotAt,
            String snapshotBy) {

        return new PayrollResultSnapshot(
                payrollTrace,
                employeeOwner,
                employeeSalaryCode,
                monetaryAmount,
                measuredQuantity,
                resultJson,
                detailJson,
                snapshotAt,
                snapshotBy);
    }
}
