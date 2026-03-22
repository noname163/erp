package com.dat.erp.entities;

import java.math.BigDecimal;

import com.dat.erp.constants.PayrollResultCalcBasis;

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
@ToString(exclude = "payrollResult")
@Entity
@Table(name = "payroll_result_detail")
public class PayrollResultDetail extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_result_code", referencedColumnName = "code", nullable = false)
    private PayrollResult payrollResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "calc_basis")
    private PayrollResultCalcBasis calcBasis;

    @Column(name = "basis_days")
    private BigDecimal basisDays;

    @Column(name = "basis_hours")
    private BigDecimal basisHours;

    @Column(name = "basis_minutes")
    private BigDecimal basisMinutes;

    @Column(name = "paid_days")
    private BigDecimal paidDays;

    @Column(name = "unpaid_days")
    private BigDecimal unpaidDays;

    @Column(name = "payable_hours")
    private BigDecimal payableHours;

    @Column(name = "payable_minutes")
    private BigDecimal payableMinutes;

    @Column(name = "expected_hours")
    private BigDecimal expectedHours;

    @Column(name = "expected_minutes")
    private BigDecimal expectedMinutes;

    @Column(name = "expected_quantity")
    private BigDecimal expectedQuantity;

    @Column(name = "payable_quantity")
    private BigDecimal payableQuantity;

    @Column(name = "rate_per_day")
    private BigDecimal ratePerDay;

    @Column(name = "rate_per_hour")
    private BigDecimal ratePerHour;

    @Column(name = "rate_per_minute")
    private BigDecimal ratePerMinute;

    @Column(name = "multiplier_applied")
    private BigDecimal multiplierApplied;

    @Column(name = "formula_note")
    private String formulaNote;

    @Column(name = "rounding_note")
    private String roundingNote;

    @Column(name = "policy_rule_code")
    private String policyRuleCode;

    @Column(name = "source_date")
    private java.time.LocalDate sourceDate;
}
