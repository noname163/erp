package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.constants.PayrollAdjustmentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@ToString(exclude = { "userProfile", "salary" })
@Entity
@Table(name = "payroll_adjustment")
public class PayrollAdjustment extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_code", referencedColumnName = "code")
    private Salary salary;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type")
    private PayrollAdjustmentType adjustmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    private ApprovalStatus approvalStatus;

    @Column(name = "amount")
    private String amount;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "unit_code")
    private String unitCode;

    @Column(name = "reason")
    private String reason;

    @Column(name = "source_month")
    private String sourceMonth;

    @Column(name = "effective_payroll_month")
    private String effectivePayrollMonth;

    @Column(name = "is_retro")
    private Boolean isRetro;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;
}
