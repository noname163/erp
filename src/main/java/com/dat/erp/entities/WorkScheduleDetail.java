package com.dat.erp.entities;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@ToString(exclude = "workSchedule")
@Entity
@Table(name = "work_schedule_detail")
public class WorkScheduleDetail extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_schedule_code", referencedColumnName = "code", nullable = false)
    private WorkSchedule workSchedule;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "is_working_day")
    private Boolean isWorkingDay;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "break_minutes")
    private Integer breakMinutes;

    @Column(name = "paid_break")
    private Boolean paidBreak;

    @Column(name = "full_day_threshold_minutes")
    private Integer fullDayThresholdMinutes;
}
