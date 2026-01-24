package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.dat.erp.constants.PayRateAppliesTo;
import com.dat.erp.constants.PayRateDayType;

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
@ToString(exclude = "policy")
@Entity
@Table(name = "pay_rate_rule")
public class PayRateRule extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_code", referencedColumnName = "code", nullable = false)
    private PayrollPolicy policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type")
    private PayRateDayType dayType;

    @Column(name = "multiplier")
    private BigDecimal multiplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to")
    private PayRateAppliesTo appliesTo;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}

