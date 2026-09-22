package com.dat.erp.services.payroll;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import com.dat.erp.exceptions.BadRequestException;

/** Dated statutory constants; sources and scope are documented in docs/vietnam-payroll.md. */
public record VietnamPayrollRules(String version, BigDecimal personalRelief, BigDecimal dependentRelief,
        BigDecimal insuranceCap, BigDecimal regionalMinimum, List<Band> taxBands, boolean fullOvertimeExemption) {
    public record Band(BigDecimal lower, BigDecimal upper, BigDecimal rate) {}
    public static VietnamPayrollRules forMonth(YearMonth month, int region) {
        if (month.isBefore(YearMonth.of(2024, 7))) {
            throw new BadRequestException("Vietnam statutory defaults are available from July 2024. Add a historical rule version for this period.");
        }
        if (region < 1 || region > 4) throw new BadRequestException("Insurance region must be 1 to 4");
        boolean modern = month.getYear() >= 2026;
        boolean july2026 = !month.isBefore(YearMonth.of(2026, 7));
        long[] floors = modern ? new long[]{5310000,4730000,4140000,3700000} : new long[]{4960000,4410000,3860000,3450000};
        long[] limits = modern ? new long[]{10000000,30000000,60000000,100000000} : new long[]{5000000,10000000,18000000,32000000,52000000,80000000};
        String[] rates = modern ? new String[]{".05",".10",".20",".30",".35"} : new String[]{".05",".10",".15",".20",".25",".30",".35"};
        List<Band> bands = new ArrayList<>();
        BigDecimal lower = BigDecimal.ZERO;
        for (int i=0; i<rates.length; i++) {
            BigDecimal upper = i<limits.length ? BigDecimal.valueOf(limits[i]) : null;
            bands.add(new Band(lower, upper, new BigDecimal(rates[i])));
            lower = upper;
        }
        return new VietnamPayrollRules(july2026 ? "VN-2026-07" : modern ? "VN-2026-01" : "VN-2024-07",
            BigDecimal.valueOf(modern ? 15500000 : 11000000), BigDecimal.valueOf(modern ? 6200000 : 4400000),
            BigDecimal.valueOf(july2026 ? 50600000 : 46800000), BigDecimal.valueOf(floors[region-1]), bands, modern);
    }
    public VietnamPayrollRules withOverrides(com.dat.erp.data.PayrollStatutorySettings s) {
        s.validateOverrides();
        List<Band> bands = taxBands;
        if (s.getTaxBands() != null) {
            bands = new ArrayList<>(); BigDecimal lower = BigDecimal.ZERO;
            for (var band : s.getTaxBands()) { bands.add(new Band(lower,band.upper(),band.rate())); lower=band.upper(); }
        }
        return new VietnamPayrollRules(version, s.getPersonalTaxRelief()==null?personalRelief:s.getPersonalTaxRelief(),
            s.getDependentTaxRelief()==null?dependentRelief:s.getDependentTaxRelief(),
            s.getInsuranceReferenceWage()==null?insuranceCap:s.getInsuranceReferenceWage().multiply(new BigDecimal("20")),
            s.getRegionalMinimumWage()==null?regionalMinimum:s.getRegionalMinimumWage(), bands, fullOvertimeExemption);
    }
}
