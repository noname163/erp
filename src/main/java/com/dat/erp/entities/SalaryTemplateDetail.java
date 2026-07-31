package com.dat.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import com.dat.erp.data.MeasuredQuantityData;
import com.dat.erp.data.MonetaryAmountData;
import com.dat.erp.data.SalaryComponentBindingData;
import com.dat.erp.utils.ErrorUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(exclude = { "salaryTemplate", "salary" })
@Entity
@Table(name = "salary_template_detail", uniqueConstraints = {
        @UniqueConstraint(name = "uk_salary_template_detail_code_company_code", columnNames = { "code", "company_code" })
})
public class SalaryTemplateDetail extends BaseAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_code", referencedColumnName = "code", nullable = false)
    private SalaryTemplate salaryTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_code", referencedColumnName = "code", nullable = false)
    private Salary salary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dependence_code", referencedColumnName = "code")
    private Salary dependenceCode;

    @Column(name = "amount")
    private String amount;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_code", referencedColumnName = "code")
    private SystemUnit unit;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_fixed")
    @Builder.Default
    private Boolean isFixed = Boolean.FALSE;

    public SalaryTemplateDetail(
            SalaryComponentBindingData componentBinding,
            MonetaryAmountData monetaryAmount,
            MeasuredQuantityData measuredQuantity,
            Integer sequenceOrder) {

        componentBinding = ErrorUtils.requireNonNull(componentBinding, "Salary template detail binding data is required");
        monetaryAmount = ErrorUtils.requireNonNull(monetaryAmount, "Salary template detail amount data is required");
        measuredQuantity = ErrorUtils.requireNonNull(measuredQuantity, "Salary template detail quantity data is required");

        this.salaryTemplate = ErrorUtils.requireNonNull(
                componentBinding.getSalaryTemplate(),
                "Salary template is required");
        this.salary = ErrorUtils.requireNonNull(
                componentBinding.getSalary(),
                "Salary is required");
        this.dependenceCode = componentBinding.getDependenceCode();
        this.amount = ErrorUtils.requireNotBlank(
                monetaryAmount.getAmount(),
                "Salary template detail amount is required");
        this.quantity = ErrorUtils.requireNonNegative(
                measuredQuantity.getQuantity(),
                "Salary template detail quantity is required",
                "Salary template detail quantity must not be negative");
        this.unit = measuredQuantity.getSystemUnit();
        this.sequenceOrder = ErrorUtils.requireNonNegative(
                sequenceOrder,
                "Salary template detail sequence order is required",
                "Salary template detail sequence order must not be negative");
        this.isFixed = componentBinding.getFixed();
    }

    public static SalaryTemplateDetail create(
            SalaryComponentBindingData componentBinding,
            MonetaryAmountData monetaryAmount,
            MeasuredQuantityData measuredQuantity,
            Integer sequenceOrder) {

        return new SalaryTemplateDetail(componentBinding, monetaryAmount, measuredQuantity, sequenceOrder);
    }
}
