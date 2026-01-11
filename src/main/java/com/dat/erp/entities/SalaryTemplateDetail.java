package com.dat.erp.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@ToString(exclude = { "salaryTemplate", "salary" })
@Entity
@Table(name = "salary_template_detail")
public class SalaryTemplateDetail extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_code", referencedColumnName = "code", nullable = false)
    private SalaryTemplate salaryTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_code", referencedColumnName = "code", nullable = false)
    private Salary salary;

    @Column(name = "component_code")
    private String componentCode;

    @Column(name = "component_name")
    private String componentName;

    @Column(name = "calculation_type")
    private String calculationType;

    @Column(name = "reference_component_code")
    private String referenceComponentCode;

    @Column(name = "percent")
    private Double percent;

    @Column(name = "cap_amount")
    private Double capAmount;

    @Column(name = "cap_percent_of")
    private Double capPercentOf;

    @Column(name = "min_amount")
    private Double minAmount;

    @Column(name = "default_amount")
    private Double defaultAmount;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;
}
