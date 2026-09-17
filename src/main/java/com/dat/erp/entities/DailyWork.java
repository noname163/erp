package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.constants.DailyWorkUnit;
import com.dat.erp.constants.DayType;
import com.dat.erp.data.ApprovalStateData;
import com.dat.erp.data.DayClassificationData;
import com.dat.erp.data.EmployeeOwnedRecordData;
import com.dat.erp.data.MeasuredQuantityData;
import com.dat.erp.utils.ErrorUtils;

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
@ToString(exclude = { "userProfile" })
@Entity
@Table(name = "daily_work")
public class DailyWork extends BaseAuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_code", referencedColumnName = "code", nullable = false)
    private UserProfile userProfile;

    @Column(name = "quantity")
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit")
    private DailyWorkUnit unit;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "ot_time")
    private Integer otTime;

    @Column(name = "used_pto")
    private Boolean usedPto;

    @Column(name = "working_date")
    private LocalDate workingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type")
    private DayType workType;

    @Column(name = "hours_worked")
    private BigDecimal hoursWorked;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status")
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Column(name = "late_minutes")
    private Integer lateMinutes;

    @Column(name = "early_leave_minutes")
    private Integer earlyLeaveMinutes;

    @Column(name = "night_hours")
    private BigDecimal nightHours;

    @Column(name = "night_overtime_hours")
    private BigDecimal nightOvertimeHours;

    @Column(name = "overtime_hours")
    private BigDecimal overtimeHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type")
    private DayType dayType;

    public DailyWork(
            EmployeeOwnedRecordData employeeOwner,
            MeasuredQuantityData measuredQuantity,
            DayClassificationData dayClassification,
            ApprovalStateData approvalState,
            LocalDate workingDate) {

        employeeOwner = ErrorUtils.requireNonNull(employeeOwner, "Daily work owner data is required");
        measuredQuantity = ErrorUtils.requireNonNull(measuredQuantity, "Daily work quantity data is required");
        dayClassification = ErrorUtils.requireNonNull(dayClassification, "Daily work day classification is required");
        approvalState = ErrorUtils.requireNonNull(approvalState, "Daily work approval state is required");

        this.userProfile = ErrorUtils.requireNonNull(
                employeeOwner.getUserProfile(),
                "Daily work user profile is required");
        this.quantity = ErrorUtils.requireNonNegative(
                measuredQuantity.getQuantity(),
                "Daily work quantity is required",
                "Daily work quantity must not be negative");
        this.workingDate = ErrorUtils.requireNonNull(
                workingDate,
                "Daily work date is required");
        this.workType = dayClassification.getWorkType();
        this.dayType = dayClassification.getDayType();
        this.approvalStatus = ErrorUtils.requireNonNull(
                approvalState.getApprovalStatus(),
                "Daily work approval status is required");
    }

    public static DailyWork create(
            EmployeeOwnedRecordData employeeOwner,
            MeasuredQuantityData measuredQuantity,
            DayClassificationData dayClassification,
            ApprovalStateData approvalState,
            LocalDate workingDate) {

        return new DailyWork(employeeOwner, measuredQuantity, dayClassification, approvalState, workingDate);
    }
}
