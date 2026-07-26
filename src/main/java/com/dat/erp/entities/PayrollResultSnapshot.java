package com.dat.erp.entities;

import java.time.LocalDateTime;

import com.dat.erp.constants.PayrollStatus;

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
}
