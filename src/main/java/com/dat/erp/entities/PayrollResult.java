package com.dat.erp.entities;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.constants.PayrollLineType;
import com.dat.erp.constants.PayrollSourceType;

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
@ToString(exclude = { "payrollRun", "employeeSummary", "userProfile", "salary", "unit", "details" })
@Entity
@Table(name = "payroll_result")
public class PayrollResult extends BaseAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_code", referencedColumnName = "code", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_summary_code", referencedColumnName = "code")
    private PayrollEmployeeSummary employeeSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_code", referencedColumnName = "code")
    private Salary salary;

    @Column(name = "amount")
    private String amount;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "quantity_value")
    private BigDecimal quantityValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_code", referencedColumnName = "code")
    private SystemUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type")
    private PayrollLineType lineType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private PayrollSourceType sourceType;

    @Column(name = "segment_from")
    private LocalDate segmentFrom;

    @Column(name = "segment_to")
    private LocalDate segmentTo;

    @Column(name = "currency")
    private String currency;

    @Column(name = "rate")
    private String rate;

    @Column(name = "multiplier")
    private BigDecimal multiplier;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "source_ref_code")
    private String sourceRefCode;

    @Column(name = "policy_snapshot_version")
    private String policySnapshotVersion;

    @Column(name = "is_manual")
    private Boolean isManual;

    @Column(name = "is_frozen")
    private Boolean isFrozen;

    @Column(name = "is_retro")
    private Boolean isRetro;

    @Column(name = "retro_reason")
    private String retroReason;

    @OneToMany(mappedBy = "payrollResult", fetch = FetchType.LAZY)
    private List<PayrollResultDetail> details;
}
