package com.dat.erp.data;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Company policy overrides. Null values select the dated statutory default. */
@Data
@Embeddable
public class PayrollStatutorySettings {
    @Pattern(regexp = "VN", message = "Only Vietnam statutory payroll is currently supported")
    @Column(name = "statutory_jurisdiction")
    private String jurisdiction = "VN";
    @DecimalMin("1.5")
    @Column(name = "weekday_ot_multiplier", precision = 24, scale = 8)
    private BigDecimal weekdayOvertimeMultiplier;
    @DecimalMin("2.0")
    @Column(name = "rest_day_ot_multiplier", precision = 24, scale = 8)
    private BigDecimal restDayOvertimeMultiplier;
    @DecimalMin("3.0")
    @Column(name = "holiday_ot_multiplier", precision = 24, scale = 8)
    private BigDecimal holidayOvertimeMultiplier;
    @DecimalMin("0.30")
    @Column(name = "night_work_premium", precision = 24, scale = 8)
    private BigDecimal nightWorkPremium;
    @DecimalMin("0")
    @Column(name = "personal_tax_relief", precision = 24, scale = 8)
    private BigDecimal personalTaxRelief;
    @DecimalMin("0")
    @Column(name = "dependent_tax_relief", precision = 24, scale = 8)
    private BigDecimal dependentTaxRelief;
    @DecimalMin("0.01")
    @Column(name = "insurance_reference_wage", precision = 24, scale = 8)
    private BigDecimal insuranceReferenceWage;
    @DecimalMin("0.01")
    @Column(name = "regional_minimum_wage", precision = 24, scale = 8)
    private BigDecimal regionalMinimumWage;
    @DecimalMin("0")
    @jakarta.validation.constraints.DecimalMax("1")
    @Column(name = "employee_social_rate", precision = 24, scale = 8)
    private BigDecimal employeeSocialRate;
    @DecimalMin("0")
    @jakarta.validation.constraints.DecimalMax("1")
    @Column(name = "employee_health_rate", precision = 24, scale = 8)
    private BigDecimal employeeHealthRate;
    @DecimalMin("0")
    @jakarta.validation.constraints.DecimalMax("1")
    @Column(name = "employee_unemployment_rate", precision = 24, scale = 8)
    private BigDecimal employeeUnemploymentRate;
    @DecimalMin("0")
    @jakarta.validation.constraints.DecimalMax("1")
    @Column(name = "employer_social_rate", precision = 24, scale = 8)
    private BigDecimal employerSocialRate;
    @DecimalMin("0")
    @jakarta.validation.constraints.DecimalMax("1")
    @Column(name = "employer_health_rate", precision = 24, scale = 8)
    private BigDecimal employerHealthRate;
    @DecimalMin("0")
    @jakarta.validation.constraints.DecimalMax("1")
    @Column(name = "employer_unemployment_rate", precision = 24, scale = 8)
    private BigDecimal employerUnemploymentRate;
    @jakarta.validation.Valid
    @jakarta.validation.constraints.Size(min = 1, max = 20)
    @jakarta.persistence.Convert(converter = com.dat.erp.utils.PayrollTaxBandsConverter.class)
    @Column(name = "monthly_tax_bands", columnDefinition = "text")
    private java.util.List<PayrollTaxBand> taxBands;
    @jakarta.validation.constraints.Size(max = 500)
    @Column(name = "statutory_override_reason", length = 500)
    private String overrideReason;

    public void validateOverrides() {
        boolean overridden = personalTaxRelief != null || dependentTaxRelief != null || insuranceReferenceWage != null
            || regionalMinimumWage != null || employeeSocialRate != null || employeeHealthRate != null
            || employeeUnemploymentRate != null || employerSocialRate != null || employerHealthRate != null
            || employerUnemploymentRate != null || taxBands != null;
        if (overridden && (overrideReason == null || overrideReason.isBlank()))
            throw new com.dat.erp.exceptions.BadRequestException("Give the legal/policy basis for tax or insurance overrides");
        if (taxBands != null) {
            BigDecimal previous = BigDecimal.ZERO;
            for (int i = 0; i < taxBands.size(); i++) {
                PayrollTaxBand band = taxBands.get(i);
                if (band == null || band.rate() == null || band.rate().signum() < 0 || band.rate().compareTo(BigDecimal.ONE) > 0
                    || (i == taxBands.size()-1 ? band.upper() != null : band.upper() == null || band.upper().compareTo(previous) <= 0))
                    throw new com.dat.erp.exceptions.BadRequestException("Tax bands must have increasing upper limits and end with an unlimited band");
                previous = band.upper();
            }
            if (taxBands.isEmpty()) throw new com.dat.erp.exceptions.BadRequestException("Tax bands cannot be empty");
        }
    }
}
