package com.dat.erp.entities;

import java.time.LocalDate;
import java.util.List;

import com.dat.erp.data.EffectivePeriodData;
import com.dat.erp.data.NamedResourceData;
import com.dat.erp.data.OperationalTextData;
import com.dat.erp.utils.ErrorUtils;

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
@ToString(exclude = { "dates" })
@Entity
@Table(name = "company_calendar")
public class CompanyCalendar extends BaseAuditableEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", nullable = false)
    private LocalDate effectiveTo;

    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "time_zone", nullable = false)
    private String timeZone;

    @Column(name = "note", nullable = false)
    private String note;

    @OneToMany(mappedBy = "calendar", fetch = FetchType.LAZY)
    private List<CalendarDate> dates;

    public CompanyCalendar(
            NamedResourceData namedResource,
            EffectivePeriodData effectivePeriod,
            OperationalTextData operationalText,
            String region,
            String timeZone) {

        namedResource = ErrorUtils.requireNonNull(namedResource, "Company calendar name data is required");
        effectivePeriod = ErrorUtils.requireNonNull(effectivePeriod, "Company calendar effective period is required");
        operationalText = ErrorUtils.requireNonNull(operationalText, "Company calendar text data is required");

        this.name = namedResource.getName();
        this.effectiveFrom = effectivePeriod.getEffectiveFrom();
        this.effectiveTo = effectivePeriod.getEffectiveTo();
        this.region = ErrorUtils.requireNotBlank(region, "Region is required");
        this.timeZone = ErrorUtils.requireNotBlank(timeZone, "Time zone is required");
        this.note = ErrorUtils.requireNotBlank(
                operationalText.getNote(),
                "Company calendar note is required");
    }

    public static CompanyCalendar create(
            NamedResourceData namedResource,
            EffectivePeriodData effectivePeriod,
            OperationalTextData operationalText,
            String region,
            String timeZone) {

        return new CompanyCalendar(namedResource, effectivePeriod, operationalText, region, timeZone);
    }
}
