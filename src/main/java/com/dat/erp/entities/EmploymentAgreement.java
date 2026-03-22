package com.dat.erp.entities;

import java.time.LocalDate;

import com.dat.erp.constants.EmploymentStatus;
import com.dat.erp.constants.EmploymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@ToString(exclude = "userProfile")
@Entity
@Table(name = "employment_agreement")
public class EmploymentAgreement extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type")
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status")
    private EmploymentStatus employmentStatus;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "suspension_from")
    private LocalDate suspensionFrom;

    @Column(name = "suspension_to")
    private LocalDate suspensionTo;

    @Column(name = "eligible_for_payroll")
    private Boolean eligibleForPayroll;

    @Column(name = "eligible_for_final_settlement")
    private Boolean eligibleForFinalSettlement;

    @Column(name = "eligible_for_retro_only")
    private Boolean eligibleForRetroOnly;

    @Column(name = "attendance_tracking_mode")
    private String attendanceTrackingMode;

    @Column(name = "notes")
    private String notes;
}
