package com.dat.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.dat.erp.constants.SalaryCalculateMethod;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "salary", uniqueConstraints = {
        @UniqueConstraint(name = "uk_salary_code_company_code", columnNames = { "code", "company_code" })
})
public class Salary extends BaseAuditableEntity {
    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculate_method")
    private SalaryCalculateMethod calculateMethod;

    @Column(name = "is_deduct")
    private Boolean isDeduct;
}
