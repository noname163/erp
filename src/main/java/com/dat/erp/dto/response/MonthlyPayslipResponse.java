package com.dat.erp.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.dat.erp.data.EmployeePayrollInputs;
import com.dat.erp.data.PayrollStatutorySettings;
import lombok.Data;

@Data
public class MonthlyPayslipResponse {
    private int schemaVersion = 1;
    private String employeeCode;
    private String period;
    private String currency;
    private String policyCode;
    private String policyName;
    private String policyEffectiveFrom;
    private String statutoryVersion;
    private String inputVersion;
    private PayrollStatutorySettings policySettings;
    private EmployeePayrollInputs inputs;
    private BigDecimal exchangeRateToVnd;
    private BigDecimal expectedAmount;
    private BigDecimal grossEarnings;
    private BigDecimal totalDeductions;
    private BigDecimal netPay;
    private BigDecimal taxableIncomeVnd;
    private Map<String, BigDecimal> hours = new LinkedHashMap<>();
    private List<Line> lines = new ArrayList<>();
    private List<String> notes = new ArrayList<>();
    public record Line(String kind, String label, BigDecimal quantity, BigDecimal rate,
            BigDecimal multiplier, BigDecimal amount, String formula, String currency) {}
}
