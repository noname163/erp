package com.dat.erp.entities;

import com.dat.erp.constants.DayType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependence_code", referencedColumnName = "code")
    private Salary dependenceCode;

    @Column(name = "day_type")
    private DayType dayType;

    @Column(name = "is_fixed")
    @Builder.Default
    private Boolean isFixed = Boolean.FALSE;

}
