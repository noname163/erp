package com.dat.erp.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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
public class EmployeeInformation {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @ManyToOne
    @JoinColumn(name = "user_code")
    private UserInformation user;

    private String nickname;

    @Convert(converter = EncryptFieldConverter.class)
    private String email;

    @Convert(converter = HashFieldConverter.class)
    private String password;

    @ManyToOne
    @JoinColumn(name = "department_code", nullable = false)
    private Department department;

    private String jobTitle;

    @Column(name = "employment_status")
    private String employmentStatus;

    private LocalDate hireDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Document> documents;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Salary> salaries;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<WorkSchedule> shifts;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmployeeIdentification> identifications;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<EmployeeDependent> dependents;

    @ManyToOne
    @JoinColumn(name = "role_code", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<AttendanceRecord> attendanceRecords;

    @ManyToOne
    @JoinColumn(name = "company_code", nullable = false)
    private Company company;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
