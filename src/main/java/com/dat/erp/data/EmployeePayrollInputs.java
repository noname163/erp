package com.dat.erp.data;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;
import lombok.Data;

/** Versioned, explicitly confirmed employee inputs; all money except FX is in salary currency. */
@Data
public class EmployeePayrollInputs {
    private String salaryCurrency;
    @NotNull private Boolean taxResident;
    @NotNull @Min(0) @Max(100) private Integer dependents;
    @NotNull @Min(1) @Max(4) private Integer insuranceRegion;
    @NotNull private Boolean socialInsurance;
    @NotNull private Boolean healthInsurance;
    @NotNull private Boolean unemploymentInsurance;
    @NotNull @DecimalMin("0") private BigDecimal insuranceSalary;
    @NotNull @DecimalMin("0") private BigDecimal taxExemptAllowances;
    @NotNull @DecimalMin("0") private BigDecimal otherTaxRelief;
    @NotNull @DecimalMin("0") private BigDecimal otherDeduction;
    @Size(max = 300) private String deductionReason;
    @DecimalMin(value = "0", inclusive = false) private BigDecimal exchangeRateToVnd;
    @Size(max = 300) private String exchangeRateSource;
    @Size(max = 300) private String insuranceExemptionReason;
    @NotNull @DecimalMin(value = "0", inclusive = false) private BigDecimal overtimeHourlyRate;
}
