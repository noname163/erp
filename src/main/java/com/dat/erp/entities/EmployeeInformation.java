package com.dat.erp.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.dat.erp.customannotation.encriptedcolumn.Encrypted;

import jakarta.persistence.*;
import lombok.*;

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

    @Encrypted(mode = Encrypted.Mode.ENCRYPT) // decryptable
    private String email;

    @Encrypted(mode = Encrypted.Mode.HASH) // non-decryptable
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
