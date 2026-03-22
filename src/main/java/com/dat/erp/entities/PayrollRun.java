package com.dat.erp.entities;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import com.dat.erp.constants.PayrollRunStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(exclude = {"results"})
@Entity
@Table(name = "payroll_run")
public class PayrollRun extends BaseAuditableEntity {

    @Column(name = "period")
    private String period;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayrollRunStatus status;

    @Column(name = "is_preview")
    private Boolean isPreview;

    @Column(name = "snapshot_version")
    private String snapshotVersion;

    @Column(name = "replayed_from_run_code")
    private String replayedFromRunCode;

    @Column(name = "approval_status")
    private String approvalStatus;

    @Column(name = "warning_count")
    private Integer warningCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "run_at")
    private LocalDateTime runAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "payrollRun", fetch = FetchType.LAZY)
    private List<PayrollEmployeeSummary> employeeSummaries;

    @OneToMany(mappedBy = "payrollRun", fetch = FetchType.LAZY)
    private List<PayrollResult> results;
}

