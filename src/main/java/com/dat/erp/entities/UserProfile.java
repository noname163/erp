package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@ToString(exclude = {
        "account", "department", "jobTitle", "identities", "skills", "salaries", "ptos"
})
@Entity
@Table(name = "user_profile")
public class UserProfile extends BaseAuditableEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<UserIdentity> identities;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<UserSkill> skills;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<Salary> salaries;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY)
    private List<EmployeePto> ptos;
}
