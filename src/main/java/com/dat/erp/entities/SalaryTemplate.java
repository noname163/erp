package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@ToString(exclude = { "details", "employeeSalaries" })
@Entity
@Table(name = "salary_template")
public class SalaryTemplate extends BaseAuditableEntity {
    private String name;

    private String description;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "currency")
    private String currency;

    @OneToMany(mappedBy = "salaryTemplate")
    private List<SalaryTemplateDetail> details;

    @OneToMany(mappedBy = "salaryTemplate")
    private List<EmployeeSalary> employeeSalaries;
}
