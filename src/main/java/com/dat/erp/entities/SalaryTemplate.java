package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.dat.erp.data.EffectivePeriodData;
import com.dat.erp.data.MonetaryAmountData;
import com.dat.erp.data.NamedResourceData;
import com.dat.erp.utils.ErrorUtils;

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

    public SalaryTemplate(
            NamedResourceData namedResource,
            MonetaryAmountData monetaryAmount,
            EffectivePeriodData effectivePeriod) {

        namedResource = ErrorUtils.requireNonNull(namedResource, "Salary template name data is required");
        monetaryAmount = ErrorUtils.requireNonNull(monetaryAmount, "Salary template amount data is required");
        effectivePeriod = ErrorUtils.requireNonNull(effectivePeriod, "Salary template effective period is required");

        this.name = namedResource.getName();
        this.description = namedResource.getDescription();
        this.totalAmount = ErrorUtils.requireNonNull(
                monetaryAmount.getDecimalTotalAmount(),
                "Salary template total amount is required");
        this.currency = ErrorUtils.requireNotBlank(
                monetaryAmount.getCurrency(),
                "Salary template currency is required");
        this.effectiveFrom = effectivePeriod.getEffectiveFrom();
        this.effectiveTo = effectivePeriod.getEffectiveTo();
    }

    public static SalaryTemplate create(
            NamedResourceData namedResource,
            MonetaryAmountData monetaryAmount,
            EffectivePeriodData effectivePeriod) {

        return new SalaryTemplate(namedResource, monetaryAmount, effectivePeriod);
    }
}
