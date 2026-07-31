package com.dat.erp.entities;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.converters.YearMonthConverter;
import com.dat.erp.utils.UuidV7;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = { "results" })
@Entity
@Table(name = "payroll_run", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_run_code_company_code", columnNames = { "code", "company_code" })
})
public class PayrollRun extends BaseAuditableEntity {

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "period", nullable = false, length = 7)
    private YearMonth period;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayrollRunStatus status = PayrollRunStatus.OPEN;;

    @Column(name = "run_at")
    private LocalDateTime runAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "payrollRun", fetch = FetchType.LAZY)
    private List<PayrollResult> results = new ArrayList<>();;

    private PayrollRun(YearMonth period) {
        assignCode("PR"+UuidV7.generate());
        this.period = Objects.requireNonNull(period);
        this.status = PayrollRunStatus.OPEN;
    }

    public static PayrollRun create(YearMonth period) {
        return new PayrollRun(period);
    }

    public void start(LocalDateTime startedAt) {
        if (status != PayrollRunStatus.OPEN) {
            throw new IllegalStateException("Only OPEN payroll run can start");
        }

        this.status = PayrollRunStatus.PROCESSING;
        this.runAt = Objects.requireNonNull(
                startedAt,
                "startedAt must not be null");
    }

    public void close(LocalDateTime closedAt) {
        if (status != PayrollRunStatus.PROCESSING) {
            throw new IllegalStateException("Only PROCESSING payroll run can close");
        }
        if (closedAt.isBefore(runAt)) {
            throw new IllegalArgumentException(
                    "closedAt must not be before runAt");
        }
        this.status = PayrollRunStatus.CLOSED;
        this.closedAt = Objects.requireNonNull(
                closedAt,
                "closedAt must not be null");
    }
    
    public void addResult(PayrollResult result) {
        Objects.requireNonNull(result);

        results.add(result);
    }

    public void completeRerun(
            int failureCount,
            boolean dryRun) {


        if (dryRun) {
            return;
        }

        this.status = failureCount > 0
                ? PayrollRunStatus.FAILED
                : PayrollRunStatus.CALCULATED;
    }

    public void markFailed(){
        this.status = PayrollRunStatus.FAILED;
    }
    public void markCaculated(){
        this.status = PayrollRunStatus.CALCULATED;
    }
}
