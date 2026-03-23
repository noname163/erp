package com.dat.erp.entities;

import java.time.LocalDate;

import com.dat.erp.constants.DayType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@ToString(exclude = "calendar")
@Entity
@Table(name = "calendar_date", uniqueConstraints = {
        @UniqueConstraint(name = "uk_calendar_date_calendar_code_cal_date", columnNames = { "calendar_code", "cal_date" })
})
public class CalendarDate extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_code", referencedColumnName = "code", nullable = false)
    private CompanyCalendar calendar;

    @Column(name = "cal_date", nullable = false)
    private LocalDate calDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false)
    private DayType dayType;

    @Column(name = "note", nullable = false)
    private String note;
}
