package com.dat.erp.entities;

import java.util.List;

import com.dat.erp.constants.PayrollSummaryStatus;

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
@ToString(exclude = { "payrollRun", "userProfile", "results" })
@Entity
@Table(name = "payroll_employee_summary")
public class PayrollEmployeeSummary extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_code", referencedColumnName = "code", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @Column(name = "gross_amount")
    private String grossAmount;

    @Column(name = "deduction_amount")
    private String deductionAmount;

    @Column(name = "net_amount")
    private String netAmount;

    @Column(name = "currency")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayrollSummaryStatus status;

    @Column(name = "has_blocking_issue")
    private Boolean hasBlockingIssue;

    @Column(name = "issue_message")
    private String issueMessage;

    @Column(name = "policy_snapshot_version")
    private String policySnapshotVersion;

    @Column(name = "is_frozen")
    private Boolean isFrozen;

    @OneToMany(mappedBy = "employeeSummary", fetch = FetchType.LAZY)
    private List<PayrollResult> results;
}
