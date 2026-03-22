package com.dat.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.dat.erp.constants.PayrollProrationBasis;
import com.dat.erp.constants.SalaryComponentType;
import com.dat.erp.constants.SalaryCalculateMethod;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "salary")
public class Salary extends BaseAuditableEntity {
    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculate_method")
    private SalaryCalculateMethod calculateMethod;

    @Column(name = "is_deduct")
    private Boolean isDeduct;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type")
    private SalaryComponentType componentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_proration_basis")
    private PayrollProrationBasis defaultProrationBasis;

    @Column(name = "taxable")
    private Boolean taxable;

    @Column(name = "manual_entry_allowed")
    private Boolean manualEntryAllowed;

    @Column(name = "multiplier_eligible")
    private Boolean multiplierEligible;
}
