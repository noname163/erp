package com.dat.erp.entities;

import com.dat.erp.constants.PayrollSourceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@ToString(exclude = { "payrollRun", "userProfile", "salary", "unit" })
@Entity
@Table(name = "payroll_result")
public class PayrollResult extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_code", referencedColumnName = "code", nullable = false)
    private PayrollRun payrollRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_code", referencedColumnName = "code", nullable = false)
    private Salary salary;

    @Column(name = "amount")
    private String amount;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_code", referencedColumnName = "code")
    private SystemUnit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type")
    private PayrollSourceType sourceType;

    @Column(name = "is_retro")
    private Boolean isRetro;

    @Column(name = "retro_reason")
    private String retroReason;
}
