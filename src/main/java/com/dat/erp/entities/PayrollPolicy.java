package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.constants.PayrollProrationBasis;

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
@ToString(exclude = { "company", "rateRules" })
@Entity
@Table(name = "payroll_policy")
public class PayrollPolicy extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_code", referencedColumnName = "code", insertable = false, updatable = false, nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "proration_basis")
    private PayrollProrationBasis prorationBasis;

    @Column(name = "standard_days_per_week")
    private Integer standardDaysPerWeek;

    @Column(name = "pay_holiday_if_off")
    private Boolean payHolidayIfOff;

    @Column(name = "rounding_rule")
    private String roundingRule;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY)
    private List<PayRateRule> rateRules;
}

