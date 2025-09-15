package com.dat.erp.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.dat.erp.customannotation.encriptedcolumn.Encrypted;

import jakarta.annotation.Generated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@ToString(exclude = { "documents", "salaries", "shifts", "identifications", "dependents", "role", "company",
        "attendanceRecords" })
@Entity
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
    @Encrypted(mode = Encrypted.Mode.ENCRYPT)
    private String email;
    @Encrypted(mode = Encrypted.Mode.HASH)
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
    private List<Document> documents;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Salary> salaries;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkSchedule> shifts;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeIdentification> identifications;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EmployeeDependent> dependents;

    @ManyToOne
    @JoinColumn(name = "role_code", nullable = false)
    private EmployeeRole role;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AttendanceRecord> attendanceRecords;

    @ManyToOne
    @JoinColumn(name = "company_code", nullable = false)
    private Company company;

    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
