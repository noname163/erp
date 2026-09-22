package com.dat.erp.data;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

/** Monthly taxable-income upper limit in VND; null is the final unlimited band. */
public record PayrollTaxBand(@DecimalMin(value = "0", inclusive = false) BigDecimal upper,
        @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal rate) {}
