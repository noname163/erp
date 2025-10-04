package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.Set;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.converters.EncryptFieldConverter;
import com.dat.erp.converters.HashFieldConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(exclude = {
        "documents", "salaries", "shifts", "identifications", "dependents",
        "role", "company", "attendanceRecords"
})
@Entity
@NamedEntityGraph(name = "Employee.basic", attributeNodes = {
        @NamedAttributeNode("role"),
        @NamedAttributeNode("company"),
        @NamedAttributeNode("department"),
        @NamedAttributeNode("user")
})
@NamedEntityGraph(name = "Employee.full", attributeNodes = {
        @NamedAttributeNode("role"),
        @NamedAttributeNode("company"),
        @NamedAttributeNode("department"),
        @NamedAttributeNode("documents"),
        @NamedAttributeNode("salaries"),
        @NamedAttributeNode("shifts"),
        @NamedAttributeNode("identifications"),
        @NamedAttributeNode("dependents"),
        @NamedAttributeNode("attendanceRecords")
})
@Table(name = "employee_informations")
public class EmployeeInformation extends BaseAuditableEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_code", referencedColumnName = "code")
    private UserInformation user;

    private String managerCode;

    private String nickname;

    @Convert(converter = EncryptFieldConverter.class)
    private String email;

    @Convert(converter = HashFieldConverter.class)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_code", referencedColumnName = "code", nullable = false)
    private Department department;

    private String jobTitle;

    @Column(name = "employment_status")
    private CommonStatus employmentStatus;

    private LocalDate hireDate;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Document> documents;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Salary> salaries;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmployeeHasWorkSchedule> shifts;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmployeeIdentification> identifications;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmployeeDependent> dependents;

    @ManyToOne
    @JoinColumn(name = "role_code", referencedColumnName = "code", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AttendanceRecord> attendanceRecords;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_code", referencedColumnName = "code", nullable = false)
    private Company company;

}
