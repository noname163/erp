package com.dat.erp.entities;

import java.util.List;

import com.dat.erp.constants.PayrollStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@ToString(exclude = { "payrollRun", "employeeSalary", "unit", "details" })
@Entity
@Table(name = "payroll_result")
public class PayrollResult extends BaseAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_code", referencedColumnName = "code", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_salary_code", referencedColumnName = "code", nullable = false)
    private EmployeeSalary employeeSalary;

    @Column(name = "expected_amount")
    private String expectedAmount;

    @Column(name = "actual_amount")
    private String actualAmount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "expected_quantity")
    private Integer expectedQuantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_code", referencedColumnName = "code")
    private SystemUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private PayrollStatus sourceType;

    @Column(name = "is_retro")
    private Boolean isRetro;

    @Column(name = "retro_reason")
    private String retroReason;

    @OneToMany(mappedBy = "payrollResult", fetch = FetchType.LAZY)
    private List<PayrollResultDetail> details;
}
