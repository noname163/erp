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
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "payroll_result_detail", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_result_detail_code_company_code", columnNames = { "code", "company_code" })
})
public class PayrollResultDetail extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_result_code", referencedColumnName = "code", nullable = false)
    private PayrollResult payrollResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "calc_basis")
    private PayrollResultCalcBasis calcBasis;

    @Column(name = "basis_days")
    private BigDecimal basisDays;

    @Column(name = "paid_days")
    private BigDecimal paidDays;

    @Column(name = "unpaid_days")
    private BigDecimal unpaidDays;

    @Column(name = "basis_hours")
    private BigDecimal basisHours;

    @Column(name = "rate_per_day")
    private BigDecimal ratePerDay;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "multiplier_applied")
    private BigDecimal multiplierApplied;

    @Column(name = "formula_note")
    private String formulaNote;
}

