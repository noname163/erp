package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dat.erp.constants.PayrollRunAuditActionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payroll_run_audit_log")
public class PayrollRunAuditLog extends BaseAuditableEntity {

    @Column(name = "payroll_run_code")
    private String payrollRunCode;

    @Column(name = "rerun_batch_code")
    private String rerunBatchCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private PayrollRunAuditActionType actionType;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "employee_code")
    private String employeeCode;

    @Column(name = "old_payroll_result_code")
    private String oldPayrollResultCode;

    @Column(name = "new_payroll_result_code")
    private String newPayrollResultCode;

    @Column(name = "old_actual_amount", precision = 19, scale = 4)
    private BigDecimal oldActualAmount;

    @Column(name = "new_actual_amount", precision = 19, scale = 4)
    private BigDecimal newActualAmount;

    @Column(name = "old_expected_amount", precision = 19, scale = 4)
    private BigDecimal oldExpectedAmount;

    @Column(name = "new_expected_amount", precision = 19, scale = 4)
    private BigDecimal newExpectedAmount;

    @Column(name = "status")
    private String status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "event_created_at")
    private LocalDateTime eventCreatedAt;
}
