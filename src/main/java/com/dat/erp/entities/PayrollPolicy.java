package com.dat.erp.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@ToString(exclude = { "employeePayrollPolicies" })
@Entity
@Table(name = "payroll_policy")
public class PayrollPolicy extends BaseAuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "standard_quantity_per_day")
    private Integer standardQuantityPerDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_code", referencedColumnName = "code")
    private SystemUnit unit;

    @Column(name ="standard_start_time")
    private LocalTime standardStartTime;

    @Column(name ="standard_end_time")
    private LocalTime standardEndTime;

    @Column(name = "rounding_rule")
    private String roundingRule;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;


    @OneToMany(mappedBy = "payrollPolicy", fetch = FetchType.LAZY)
    private List<EmployeePayrollPolicy> employeePayrollPolicies;
}
