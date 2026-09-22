package com.dat.erp.services.payroll;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.dat.erp.constants.*;
import com.dat.erp.data.*;
import com.dat.erp.dto.response.MonthlyPayslipResponse;
import com.dat.erp.entities.*;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.salary.calculation.*;
import com.dat.erp.utils.*;
import com.dat.erp.testutils.EntityTestData;

class VietnamPayslipTest {
    private CalculateVietnamPayslipStep calculator;
    private MonthlySalaryCalculationContext context;
    private EmployeePayrollInputs inputs;
    private PayrollStatutorySettings policy;
    private static BigDecimal d(String s) { return new BigDecimal(s); }
    @BeforeEach void setup() {
        SecurityContextService security=mock(SecurityContextService.class);
        when(security.getCurrentCompanySecretKey()).thenReturn("test-secret");
        calculator=new CalculateVietnamPayslipStep(mock(PayrollInputService.class),security);
        context=new MonthlySalaryCalculationContext("EMP-1","COMPANY-1",YearMonth.of(2026,9));
        EmployeeSalary salary=new EmployeeSalary(); salary.setCurrency("VND"); salary.setSalaryBasisType(SalaryBasisType.MONTHLY);
        salary.setTotalAmount(CompanySecretKeyCryptoUtils.encrypt("32000000","test-secret")); context.setEmployeeSalary(salary);
        PayrollPolicy pp=new PayrollPolicy(); pp.setName("Vietnam"); pp.setEffectiveFrom(LocalDate.of(2026,1,1)); context.setPayrollPolicy(pp);
        context.setExpectedWorkingHourPerMonth(d("160")); context.setActualWorkingHourPerMonth(d("160"));
        context.setDetails(List.of()); context.setDailyWorks(List.of());
        inputs=new EmployeePayrollInputs(); inputs.setTaxResident(true); inputs.setDependents(0); inputs.setInsuranceRegion(1);
        inputs.setSocialInsurance(true); inputs.setHealthInsurance(true); inputs.setUnemploymentInsurance(true);
        inputs.setInsuranceSalary(d("32000000")); inputs.setOvertimeHourlyRate(d("200000"));
        inputs.setTaxExemptAllowances(BigDecimal.ZERO); inputs.setOtherTaxRelief(BigDecimal.ZERO); inputs.setOtherDeduction(BigDecimal.ZERO);
        policy=new PayrollStatutorySettings();
    }
    private MonthlyPayslipResponse calculate() { return calculator.calculate(context,policy,inputs,"INPUT-1"); }
    private void eq(String expected,BigDecimal actual) { assertEquals(0,d(expected).compareTo(actual),actual.toString()); }
    private DailyWork work(DayType type,String hours,String ot,String night,String nightOt) {
        DailyWork w=new DailyWork(); w.setWorkingDate(LocalDate.of(2026,9,1));w.setWorkType(type);
        w.setHoursWorked(d(hours));w.setOvertimeHours(d(ot));w.setNightHours(d(night));w.setNightOvertimeHours(d(nightOt));return w;
    }
    @Test void residentPayslipReconcilesAndExcludesEmployerContributions() {
        var p=calculate(); eq("32000000",p.getGrossEarnings());eq("13140000",p.getTaxableIncomeVnd());eq("27826000",p.getNetPay());
        eq("4174000",p.getTotalDeductions()); assertTrue(p.getLines().stream().anyMatch(l->l.kind().equals("EMPLOYER")));
        eq(p.getNetPay().toPlainString(),p.getGrossEarnings().subtract(p.getTotalDeductions()));
    }
    @Test void weekdayOvertimeUses150PercentAnd2026Exemption() {
        context.setDailyWorks(List.of(work(DayType.NORMAL,"8","10","0","0")));
        var p=calculate();eq("35000000",p.getGrossEarnings());eq("30826000",p.getNetPay());eq("13140000",p.getTaxableIncomeVnd());
    }
    @Test void nightOvertimeUsesDaytimeOvertimeRateWhenBothOccur() {
        context.setDailyWorks(List.of(work(DayType.NORMAL,"8","3","0","2")));
        var p=calculate();eq("33140000",p.getGrossEarnings());
        assertTrue(p.getLines().stream().anyMatch(l->l.label().contains("night overtime") && l.multiplier().compareTo(d("2.1"))==0));
    }
    @Test void paidLeaveIsIncludedOnceAndUnpaidTimeIsNotDeductedTwice() {
        context.setPaidLeaveHours(d("8"));context.setUnpaidLeaveHours(d("8"));context.setActualWorkingHourPerMonth(d("152"));
        var p=calculate();eq("30400000",p.getGrossEarnings());
        assertFalse(p.getLines().stream().anyMatch(l->l.kind().equals("DEDUCTION") && l.label().contains("leave")));
    }
    @Test void insuranceCapsAndDependentsApply() {
        inputs.setInsuranceSalary(d("100000000"));inputs.setInsuranceRegion(4);inputs.setDependents(2);
        var p=calculate();
        eq("4048000",p.getLines().stream().filter(l->l.label().equals("Social insurance")).findFirst().orElseThrow().amount());
        eq("740000",p.getLines().stream().filter(l->l.label().equals("Unemployment insurance")).findFirst().orElseThrow().amount());
        eq("0",p.getTaxableIncomeVnd());
    }
    @Test void statutoryVersionsChangeWithPayrollPeriod() {
        eq("46800000",VietnamPayrollRules.forMonth(YearMonth.of(2026,6),1).insuranceCap());
        eq("50600000",VietnamPayrollRules.forMonth(YearMonth.of(2026,7),1).insuranceCap());
        eq("11000000",VietnamPayrollRules.forMonth(YearMonth.of(2025,12),1).personalRelief());
        assertEquals(7,VietnamPayrollRules.forMonth(YearMonth.of(2025,12),1).taxBands().size());
        assertEquals(5,VietnamPayrollRules.forMonth(YearMonth.of(2026,1),1).taxBands().size());
    }
    @Test void nonResidentHasNoPersonalRelief() {
        inputs.setTaxResident(false);var p=calculate();eq("32000000",p.getTaxableIncomeVnd());eq("22240000",p.getNetPay());
    }
    @Test void foreignSalaryNeedsExplicitConversion() {
        context.getEmployeeSalary().setCurrency("USD");assertThrows(BadRequestException.class,this::calculate);
    }
    @Test void snapshotRoundTripDoesNotRecalculateWhenPolicyChanges() {
        var original=calculate();String json=PayslipJson.write(original);inputs.setDependents(3);policy.setWeekdayOvertimeMultiplier(d("2"));
        var saved=PayslipJson.read(json,MonthlyPayslipResponse.class);eq("27826000",saved.getNetPay());assertEquals(0,saved.getInputs().getDependents());
    }
    @Test void salaryDeductionsHaveNegativeEffectAndNamedFormula() {
        Salary definition=new Salary();EntityTestData.setCode(definition,"DEDUCT");definition.setName("Advance repayment");definition.setIsDeduct(true);definition.setCalculateMethod(SalaryCalculateMethod.FIXED);
        EmployeeSalaryDetail deduction=new EmployeeSalaryDetail();deduction.setSalary(definition);deduction.setAmount("1000000");
        Salary base=new Salary();EntityTestData.setCode(base,"BASE");base.setName("Base salary");base.setCalculateMethod(SalaryCalculateMethod.FIXED);base.setIsDeduct(false);
        EmployeeSalaryDetail earning=new EmployeeSalaryDetail();earning.setSalary(base);earning.setAmount("32000000");earning.setIsFixed(false);
        context.setDetails(List.of(earning,deduction));var p=calculate();eq("26826000",p.getNetPay());
    }
    @Test void invalidNightSubsetFailsInsteadOfOverpaying() {
        context.setDailyWorks(List.of(work(DayType.NORMAL,"8","1","0","2")));
        assertThrows(BadRequestException.class,this::calculate);
    }
    @Test void companyOvertimeAboveStatutoryLimitRemainsTaxable() {
        policy.setWeekdayOvertimeMultiplier(d("2"));
        context.setDailyWorks(List.of(work(DayType.NORMAL,"8","10","0","0")));
        var p=calculate();eq("36000000",p.getGrossEarnings());eq("14140000",p.getTaxableIncomeVnd());
    }
    @Test void approvedTaxAndInsuranceOverridesAreSavedAndUsed() {
        policy.setOverrideReason("New approved company policy");policy.setPersonalTaxRelief(d("16000000"));
        policy.setEmployeeSocialRate(d(".09"));
        policy.setTaxBands(List.of(new PayrollTaxBand(d("10000000"),d(".05")),new PayrollTaxBand(null,d(".15"))));
        var p=calculate();eq("12320000",p.getTaxableIncomeVnd());
        assertEquals("New approved company policy",p.getPolicySettings().getOverrideReason());
    }
    @Test void taxOverrideRequiresReasonAndOrderedBands() {
        policy.setPersonalTaxRelief(d("16000000"));assertThrows(BadRequestException.class,this::calculate);
        policy.setOverrideReason("Test");policy.setTaxBands(List.of(new PayrollTaxBand(null,d(".05")),new PayrollTaxBand(d("1"),d(".10"))));
        assertThrows(BadRequestException.class,this::calculate);
    }
    @Test void pre2026OvertimeExemptsOnlyPremium() {
        context.setMonth(YearMonth.of(2025,9));context.setDailyWorks(List.of(work(DayType.NORMAL,"8","10","0","0")));
        var p=calculate();eq("19640000",p.getTaxableIncomeVnd());
    }
    @Test void paidLeaveAndLateHoursRemainFractional() {
        context.setPaidLeaveHours(d("0.5"));context.setLateEarlyDeductionHours(d("0.25"));context.setActualWorkingHourPerMonth(d("159.75"));
        var p=calculate();eq("31950000",p.getGrossEarnings());eq("0.25",p.getHours().get("Late arrival / early departure"));
    }
    @Test void holidayWorkAddsOnlyPremiumToIncludedBaseHours() {
        context.setDailyWorks(List.of(work(DayType.HOLIDAY_WORK,"8","0","0","0")));
        eq("35200000",calculate().getGrossEarnings());
    }
    @Test void ambiguousHolidayAndLeaveOvertimeRequireCorrectClassification() {
        context.setDailyWorks(List.of(work(DayType.HOLIDAY,"8","0","0","0")));
        assertThrows(BadRequestException.class,this::calculate);
        context.setDailyWorks(List.of(work(DayType.PTO_PAID,"8","1","0","0")));
        assertThrows(BadRequestException.class,this::calculate);
    }
}
