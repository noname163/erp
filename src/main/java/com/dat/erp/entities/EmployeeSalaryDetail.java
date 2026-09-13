package com.dat.erp.entities;

import com.dat.erp.constants.DayType;
import com.dat.erp.constants.SalaryUnitType;
import com.dat.erp.data.DayClassificationData;
import com.dat.erp.data.MonetaryAmountData;
import com.dat.erp.data.SalaryComponentBindingData;
import com.dat.erp.utils.ErrorUtils;

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
@ToString(exclude = { "salary", "employeeSalary" })
@Entity
@Table(name = "employee_salary_detail", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_salary_detail_code_company_code", columnNames = { "code", "company_code" })
})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_unit_type")
    @Builder.Default
    private SalaryUnitType unitType = SalaryUnitType.HOUR;

    public EmployeeSalaryDetail(
            SalaryComponentBindingData componentBinding,
            MonetaryAmountData monetaryAmount,
            DayClassificationData dayClassification,
            SalaryUnitType unitType) {

        componentBinding = ErrorUtils.requireNonNull(componentBinding, "Employee salary detail binding data is required");
        monetaryAmount = ErrorUtils.requireNonNull(monetaryAmount, "Employee salary detail amount data is required");
        dayClassification = ErrorUtils.requireNonNull(dayClassification, "Employee salary detail day classification is required");

        this.salary = ErrorUtils.requireNonNull(
                componentBinding.getSalary(),
                "Salary is required");
        this.employeeSalary = ErrorUtils.requireNonNull(
                componentBinding.getEmployeeSalary(),
                "Employee salary is required");
        this.amount = ErrorUtils.requireNotBlank(
                monetaryAmount.getAmount(),
                "Employee salary detail amount is required");
        this.dependenceCode = componentBinding.getDependenceCode();
        this.dayType = ErrorUtils.requireNonNull(
                dayClassification.getDayType(),
                "Employee salary detail day type is required");
        this.isFixed = componentBinding.getFixed();
        this.unitType = unitType == null ? SalaryUnitType.HOUR : unitType;
    }

    public static EmployeeSalaryDetail create(
            SalaryComponentBindingData componentBinding,
            MonetaryAmountData monetaryAmount,
            DayClassificationData dayClassification,
            SalaryUnitType unitType) {

        return new EmployeeSalaryDetail(componentBinding, monetaryAmount, dayClassification, unitType);
    }
}
