package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@ToString(exclude = "details")
@Entity
@Table(name = "work_schedule")
public class WorkSchedule extends BaseAuditableEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "schedule_type")
    private String scheduleType;

    @Column(name = "standard_hours_per_day")
    private Integer standardHoursPerDay;

    @Column(name = "standard_minutes_per_day")
    private Integer standardMinutesPerDay;

    @Column(name = "flexible_working_hours")
    private Boolean flexibleWorkingHours;

    @Column(name = "cross_midnight_allowed")
    private Boolean crossMidnightAllowed;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @OneToMany(mappedBy = "workSchedule", fetch = FetchType.LAZY)
    private List<WorkScheduleDetail> details;
}
