package com.dat.erp.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.dat.erp.constants.PayrollProrationBasis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@ToString(exclude = { "rateRules" })
@Entity
@Table(name = "payroll_policy")
public class PayrollPolicy extends BaseAuditableEntity {

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "proration_basis")
    private PayrollProrationBasis prorationBasis;

    @Column(name = "standard_days_per_week")
    private Integer standardDaysPerWeek;

    @Column(name = "pay_holiday_if_off")
    private Boolean payHolidayIfOff;

    @Column(name = "rounding_rule")
    private String roundingRule;

    @Column(name = "standard_hours_per_day")
    private Integer standardHoursPerDay;

    @Column(name = "standard_minutes_per_day")
    private Integer standardMinutesPerDay;

    @Column(name = "rounding_mode")
    private String roundingMode;

    @Column(name = "round_at")
    private String roundAt;

    @Column(name = "zero_denominator_action")
    private String zeroDenominatorAction;

    @Column(name = "holiday_weekend_overlap_rule")
    private String holidayWeekendOverlapRule;

    @Column(name = "approval_mode")
    private String approvalMode;

    @Column(name = "freeze_snapshot_required")
    private Boolean freezeSnapshotRequired;

    @Column(name = "night_premium_start")
    private LocalTime nightPremiumStart;

    @Column(name = "night_premium_end")
    private LocalTime nightPremiumEnd;

    @Column(name = "ot_requires_approval")
    private Boolean otRequiresApproval;

    @Column(name = "ot_minimum_minutes")
    private Integer otMinimumMinutes;

    @Column(name = "ot_rounding_minutes")
    private Integer otRoundingMinutes;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY)
    private List<PayRateRule> rateRules;
}

