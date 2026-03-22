package com.dat.erp.entities;

import com.dat.erp.constants.DailyWorkWorkType;
import com.dat.erp.constants.PayrollProrationBasis;
import com.dat.erp.constants.SalaryComponentType;

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
@ToString(exclude = { "salary", "employeeSalary" })
@Entity
@Table(name = "employee_salary_detail")
public class EmployeeSalaryDetail extends BaseAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_code", referencedColumnName = "code", nullable = false)
    private Salary salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_salary_code", referencedColumnName = "code", nullable = false)
    private EmployeeSalary employeeSalary;

    @Column(name = "amount")
    private String amount;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "proration_basis_override")
    private PayrollProrationBasis prorationBasisOverride;

    @Column(name = "is_prorated")
    private Boolean isProrated;

    @Column(name = "is_multiplier_eligible")
    private Boolean isMultiplierEligible;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type")
    private SalaryComponentType componentType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependence_code", referencedColumnName = "code")
    private Salary dependenceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "daily_work_work_type")
    private DailyWorkWorkType dailyWorkWorkType;

}
