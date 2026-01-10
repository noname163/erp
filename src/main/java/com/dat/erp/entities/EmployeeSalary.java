package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Entity
@Table(name = "employee_salary")
@NamedEntityGraph(name = "EmployeeSalary.full", attributeNodes = {
        @NamedAttributeNode("userProfile"),
        @NamedAttributeNode("details")
})
public class EmployeeSalary extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @ManyToOne
    @JoinColumn(name = "template_code", referencedColumnName = "code")
    private SalaryTemplate salaryTemplate;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String totalAmount;
    private String currency;

    @OneToMany(mappedBy = "employeeSalary")
    private List<EmployeeSalaryDetail> details;
}
