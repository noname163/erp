package com.dat.erp.entities;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.data.PayrollResultSnapshotData;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.exceptions.UnauthorizedException;
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

    public static PayrollResultSnapshot create(
            PayrollResultSnapshotData data) {

        return new PayrollResultSnapshot(data);
    }
}
