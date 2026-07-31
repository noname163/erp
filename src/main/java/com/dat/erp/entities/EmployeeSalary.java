package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.data.EffectivePeriodData;
import com.dat.erp.data.EmployeeOwnedRecordData;
import com.dat.erp.data.MonetaryAmountData;
import com.dat.erp.utils.ErrorUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.dat.erp.constants.SalaryBasisType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(exclude = { "userProfile", "salaryTemplate", "details" })
@Entity
@Table(name = "employee_salary", uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_salary_code_company_code", columnNames = { "code", "company_code" })
})
public class EmployeeSalary extends BaseAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_code", referencedColumnName = "code")
    private SalaryTemplate salaryTemplate;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "total_amount")
    private String totalAmount;

    @Column(name = "currency")
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_basis_type")
    @Builder.Default
    private SalaryBasisType salaryBasisType = SalaryBasisType.WORKING_HOUR;

    @OneToMany(mappedBy = "employeeSalary", fetch = FetchType.LAZY)
    private List<EmployeeSalaryDetail> details;

    public EmployeeSalary(
            EmployeeOwnedRecordData employeeOwner,
            SalaryTemplate salaryTemplate,
            MonetaryAmountData monetaryAmount,
            EffectivePeriodData effectivePeriod,
            SalaryBasisType salaryBasisType) {

        employeeOwner = ErrorUtils.requireNonNull(employeeOwner, "Employee salary owner data is required");
        monetaryAmount = ErrorUtils.requireNonNull(monetaryAmount, "Employee salary amount data is required");
        effectivePeriod = ErrorUtils.requireNonNull(effectivePeriod, "Employee salary effective period is required");

        this.userProfile = ErrorUtils.requireNonNull(
                employeeOwner.getUserProfile(),
                "Employee salary user profile is required");
        this.salaryTemplate = salaryTemplate;
        this.totalAmount = ErrorUtils.requireNotBlank(
                monetaryAmount.getTotalAmount(),
                "Employee salary total amount is required");
        this.currency = ErrorUtils.requireNotBlank(
                monetaryAmount.getCurrency(),
                "Employee salary currency is required");
        this.effectiveFrom = effectivePeriod.getEffectiveFrom();
        this.effectiveTo = effectivePeriod.getEffectiveTo();
        this.salaryBasisType = salaryBasisType == null ? SalaryBasisType.WORKING_HOUR : salaryBasisType;
    }

    public static EmployeeSalary create(
            EmployeeOwnedRecordData employeeOwner,
            SalaryTemplate salaryTemplate,
            MonetaryAmountData monetaryAmount,
            EffectivePeriodData effectivePeriod,
            SalaryBasisType salaryBasisType) {

        return new EmployeeSalary(employeeOwner, salaryTemplate, monetaryAmount, effectivePeriod, salaryBasisType);
    }
}
