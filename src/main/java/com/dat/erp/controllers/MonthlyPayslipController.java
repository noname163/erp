package com.dat.erp.controllers;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.dat.erp.data.EmployeePayrollInputs;
import com.dat.erp.dto.response.MonthlyPayslipResponse;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.exceptions.ResourceNotFoundException;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollInputService;
import com.dat.erp.services.payroll.VietnamPayrollRules;
import com.dat.erp.utils.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payroll-payslips")
@RequiredArgsConstructor
@PreAuthorize("hasRoles({'HUMAN_RESOURCES', 'COMPANY_MANAGER', 'ADMIN', 'SYSTEM_ADMIN'})")
public class MonthlyPayslipController {
    private final PayrollResultRepository results;
    private final SecurityContextService security;
    private final PayrollInputService inputs;
    public record SetupRequest(@NotNull LocalDate effectiveFrom, @NotNull @Valid EmployeePayrollInputs inputs) {}

    @GetMapping("/{code}")
    @Transactional(readOnly = true)
    public Map<String,Object> get(@PathVariable String code) {
        PayrollResult result = scoped(code);
        String employee = result.getEmployeeSalary().getUserProfile().getCode();
        var period = result.getPayrollRun().getPeriod();
        String currency = resolveCurrency(result);
        Map<String,Object> response = new LinkedHashMap<>();
        response.put("employeeCode", employee);
        response.put("period", period.toString());
        response.put("currency", currency);
        response.put("payslip", result.getPayslipSnapshot() == null ? null : PayslipJson.read(
            CompanySecretKeyCryptoUtils.decrypt(result.getPayslipSnapshot(), security.getCurrentCompanySecretKey()), MonthlyPayslipResponse.class));
        var version = inputs.find(security.getCurrentCompanyCode(), employee, period.atDay(1));
        response.put("defaults", vietnamDefaults(result, currency));
        response.put("vietnamRegions", vietnamRegions(period));
        response.put("inputs", version == null ? null : inputs.decode(version));
        response.put("inputsEffectiveFrom", version == null ? null : version.getEffectiveFrom());
        response.put("message", result.getCalculationError() != null ? result.getCalculationError() : result.getPayslipSnapshot() == null ? "No saved monthly calculation is available. Confirm payroll setup, then rerun this employee's payroll to generate a complete payslip." : null);
        return response;
    }

    @PostMapping("/{code}/configuration")
    @Transactional
    public Map<String,String> configure(@PathVariable String code, @Valid @RequestBody SetupRequest request) {
        PayrollResult result = scoped(code);
        String currency = resolveCurrency(result);
        if (!"VND".equalsIgnoreCase(currency) && (request.inputs().getExchangeRateToVnd() == null
                || request.inputs().getExchangeRateSource() == null || request.inputs().getExchangeRateSource().isBlank()))
            throw new com.dat.erp.exceptions.BadRequestException("Enter the exchange rate to VND and its source");
        request.inputs().setSalaryCurrency(currency);
        inputs.save(result.getEmployeeSalary().getUserProfile().getCode(), request.effectiveFrom(), request.inputs());
        return Map.of("message", "Payroll setup saved. Rerun payroll to apply it; saved payslips remain unchanged.");
    }
    private PayrollResult scoped(String code) {
        return results.findByCodeAndCompanyCodeAndIsDeletedFalse(code, security.getCurrentCompanyCode())
            .orElseThrow(() -> new ResourceNotFoundException("Payroll result not found"));
    }

    private String resolveCurrency(PayrollResult result) {
        return result.getCurrency() == null || result.getCurrency().isBlank()
                ? result.getEmployeeSalary().getCurrency()
                : result.getCurrency();
    }

    private EmployeePayrollInputs vietnamDefaults(PayrollResult result, String currency) {
        BigDecimal salary = new BigDecimal(CompanySecretKeyCryptoUtils.decrypt(
                result.getEmployeeSalary().getTotalAmount(), security.getCurrentCompanySecretKey()));
        BigDecimal expectedHours = result.getExpectedQuantity() == null || result.getExpectedQuantity() <= 0
                ? new BigDecimal("176")
                : BigDecimal.valueOf(result.getExpectedQuantity());
        EmployeePayrollInputs defaults = new EmployeePayrollInputs();
        defaults.setSalaryCurrency(currency);
        defaults.setTaxResident(true);
        defaults.setDependents(0);
        defaults.setInsuranceRegion(1);
        defaults.setSocialInsurance(true);
        defaults.setHealthInsurance(true);
        defaults.setUnemploymentInsurance(true);
        defaults.setInsuranceSalary(salary);
        defaults.setOvertimeHourlyRate(salary.divide(expectedHours, 12, RoundingMode.HALF_UP));
        defaults.setTaxExemptAllowances(BigDecimal.ZERO);
        defaults.setOtherTaxRelief(BigDecimal.ZERO);
        defaults.setOtherDeduction(BigDecimal.ZERO);
        if ("VND".equalsIgnoreCase(currency)) {
            defaults.setExchangeRateToVnd(BigDecimal.ONE);
            defaults.setExchangeRateSource("Salary currency is VND");
        }
        return defaults;
    }

    private java.util.List<Map<String, Object>> vietnamRegions(java.time.YearMonth period) {
        String[] descriptions = {
                "Major designated urban areas, including central Hanoi and Ho Chi Minh City",
                "Designated provincial cities, towns and industrial districts",
                "Other designated districts with an intermediate statutory minimum",
                "Remaining localities not listed in Regions I, II or III"
        };
        java.util.List<Map<String, Object>> regions = new java.util.ArrayList<>();
        for (int region = 1; region <= 4; region++) {
            VietnamPayrollRules rules = VietnamPayrollRules.forMonth(period, region);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("region", region);
            item.put("description", descriptions[region - 1]);
            item.put("minimumWageVnd", rules.regionalMinimum());
            item.put("ruleVersion", rules.version());
            regions.add(item);
        }
        return regions;
    }
}
