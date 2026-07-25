package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.dat.erp.constants.PayrollRunAuditActionType;
import com.dat.erp.constants.PayrollRunAuditStatus;
import com.dat.erp.utils.UuidV7;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payroll_run_audit_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PayrollRunAuditLog extends BaseAuditableEntity {

    @Column(name = "payroll_run_code")
    private String payrollRunCode;

    public PayrollRunAuditLog(String payrollRunCode, String rerunBatchCode, PayrollRunAuditActionType actionType,
             String reason, String employeeCode, String oldPayrollResultCode,
            String newPayrollResultCode, BigDecimal oldActualAmount, BigDecimal newActualAmount,
            BigDecimal oldExpectedAmount, BigDecimal newExpectedAmount,
            String errorMessage, String requestId, String traceId, LocalDateTime eventCreatedAt) {
        assignCode("PRRAL" + UuidV7.generate());
        this.payrollRunCode = payrollRunCode;
        this.rerunBatchCode = rerunBatchCode;
        this.actionType = actionType;
        this.reason = reason;
        this.employeeCode = employeeCode;
        this.oldPayrollResultCode = oldPayrollResultCode;
        this.newPayrollResultCode = newPayrollResultCode;
        this.oldActualAmount = oldActualAmount;
        this.newActualAmount = newActualAmount;
        this.oldExpectedAmount = oldExpectedAmount;
        this.newExpectedAmount = newExpectedAmount;
        this.errorMessage = errorMessage;
        this.requestId = requestId;
        this.traceId = traceId;
        this.eventCreatedAt = eventCreatedAt;
    }

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayrollRunAuditStatus status;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "request_id")
    private String requestId;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "event_created_at")
    private LocalDateTime eventCreatedAt;

    public void assignRequestedPersonCode(String code){
        this.requestedBy = code;
    }
}
