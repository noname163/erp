package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.dat.erp.constants.PayRateAppliesTo;
import com.dat.erp.constants.PayRateDayType;
import com.dat.erp.constants.EmploymentType;
import com.dat.erp.constants.PayrollRateRuleType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "rate_type")
    private PayrollRateRuleType rateType;

    @Column(name = "multiplier")
    private BigDecimal multiplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "applies_to")
    private PayRateAppliesTo appliesTo;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "rate_name")
    private String rateName;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "minimum_minutes")
    private Integer minimumMinutes;

    @Column(name = "rounding_minutes")
    private Integer roundingMinutes;

    @Column(name = "requires_approval")
    private Boolean requiresApproval;

    @Column(name = "department_code")
    private String departmentCode;

    @Column(name = "location_code")
    private String locationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Column(name = "salary_code")
    private String salaryCode;

    @Column(name = "is_stackable")
    private Boolean isStackable;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;
}
