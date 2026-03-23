package com.dat.erp.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.dat.erp.constants.DailyWorkUnit;
import com.dat.erp.constants.DateType;

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
    private DateType workType;

    @Column(name = "hours_worked")
    private BigDecimal hoursWorked;
}
