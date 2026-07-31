package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@ToString(exclude = {
        "account", "department", "jobTitle", "identities", "skills", "employeeSalaries", "ptos", "dailyWorks",
        "employeePayrollPolicies"
})
@Entity
@Table(name = "user_profile", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_profile_code_company_code", columnNames = { "code", "company_code" })
})
public class UserProfile extends BaseAuditableEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_code", referencedColumnName = "code")
    private Account account;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", referencedColumnName = "code")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_title_code", referencedColumnName = "code")
    private JobTitle jobTitle;

    @Column(name = "manager_code")
    private String managerCode;

    @Column(name = "employee_number")
    private String employeeNumber;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<UserIdentity> identities;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<UserSkill> skills;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<EmployeeSalary> employeeSalaries;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<EmployeePto> ptos;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<DailyWork> dailyWorks;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<EmployeePayrollPolicy> employeePayrollPolicies;
}
