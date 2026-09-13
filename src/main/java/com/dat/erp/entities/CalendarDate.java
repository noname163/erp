package com.dat.erp.entities;

import java.time.LocalDate;

import com.dat.erp.constants.DayType;
import com.dat.erp.data.DayClassificationData;
import com.dat.erp.data.OperationalTextData;
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
@ToString(exclude = "calendar")
@Entity
@Table(name = "calendar_date")
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

    public CalendarDate(
            CompanyCalendar calendar,
            LocalDate calDate,
            DayClassificationData dayClassification,
            OperationalTextData operationalText) {

        dayClassification = ErrorUtils.requireNonNull(dayClassification, "Calendar date day classification is required");
        operationalText = ErrorUtils.requireNonNull(operationalText, "Calendar date text data is required");

        this.calendar = ErrorUtils.requireNonNull(calendar, "Calendar is required");
        this.calDate = ErrorUtils.requireNonNull(calDate, "Calendar date is required");
        this.dayType = ErrorUtils.requireNonNull(
                dayClassification.getDayType(),
                "Calendar date day type is required");
        this.note = ErrorUtils.requireNotBlank(
                operationalText.getNote(),
                "Calendar date note is required");
    }

    public static CalendarDate create(
            CompanyCalendar calendar,
            LocalDate calDate,
            DayClassificationData dayClassification,
            OperationalTextData operationalText) {

        return new CalendarDate(calendar, calDate, dayClassification, operationalText);
    }
}
