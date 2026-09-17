package com.dat.erp.services.salary.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.dat.erp.constants.*;
import com.dat.erp.data.*;
import com.dat.erp.dto.response.MonthlyPayslipResponse;
import com.dat.erp.dto.response.MonthlyPayslipResponse.Line;
import com.dat.erp.entities.*;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.*;
import com.dat.erp.utils.CompanySecretKeyCryptoUtils;
import lombok.RequiredArgsConstructor;

@Component
@Order(48)
@RequiredArgsConstructor
public class CalculateVietnamPayslipStep implements MonthlySalaryCalculationStep {
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private final PayrollInputService inputService;
    private final SecurityContextService security;

    @Override
    public void execute(MonthlySalaryCalculationContext c) {
        PayrollStatutorySettings settings = c.getPayrollPolicy().getStatutorySettings();
        if (settings == null) settings = new PayrollStatutorySettings();
        if (!"VN".equals(settings.getJurisdiction())) throw new BadRequestException("Configure a supported statutory jurisdiction (VN) on the employee payroll policy");
        EmployeePayrollInputVersion version = inputService.find(c.getCompanyCode(), c.getEmployeeCode(), c.getMonth().atDay(1));
        if (version == null) throw new BadRequestException("Payroll setup required: confirm residency, dependents, insurance and overtime wage for " + c.getEmployeeCode() + " in View detail > Payroll setup");
        EmployeePayrollInputs inputs = inputService.decode(version);
        if (!c.getEmployeeSalary().getCurrency().equalsIgnoreCase(inputs.getSalaryCurrency()))
            throw new BadRequestException("Salary currency changed. Confirm a new payroll input version in the salary currency.");
        if (!"VND".equalsIgnoreCase(inputs.getSalaryCurrency()) && !version.getEffectiveFrom().equals(c.getMonth().atDay(1)))
            throw new BadRequestException("Confirm the VND exchange rate in a payroll input version for this month.");
        MonthlyPayslipResponse p = calculate(c, settings, inputs, version.getCode());
        c.setPayslip(p);
        c.setFinalSalary(p.getNetPay());
        c.setAuditTrail(new ArrayList<>());
    }

    public MonthlyPayslipResponse calculate(MonthlySalaryCalculationContext c, PayrollStatutorySettings settings,
            EmployeePayrollInputs in, String inputVersion) {
        VietnamPayrollRules rules = VietnamPayrollRules.forMonth(c.getMonth(), in.getInsuranceRegion()).withOverrides(settings);
        String currency = c.getEmployeeSalary().getCurrency();
        boolean vnd = "VND".equalsIgnoreCase(currency);
        BigDecimal fx = vnd ? BigDecimal.ONE : in.getExchangeRateToVnd();
        if (fx == null || fx.signum() <= 0 || (!vnd && (in.getExchangeRateSource() == null || in.getExchangeRateSource().isBlank())))
            throw new BadRequestException("A positive VND exchange rate and source are required for " + currency);
        MonthlyPayslipResponse p = new MonthlyPayslipResponse();
        p.setEmployeeCode(c.getEmployeeCode()); p.setPeriod(c.getMonth().toString()); p.setCurrency(currency);
        p.setPolicyCode(c.getPayrollPolicy().getCode()); p.setPolicyName(c.getPayrollPolicy().getName());
        p.setPolicyEffectiveFrom(String.valueOf(c.getPayrollPolicy().getEffectiveFrom()));
        p.setPolicySettings(settings); p.setInputs(in); p.setInputVersion(inputVersion);
        if (settings.getOverrideReason() != null) p.getNotes().add("Company statutory override basis: " + settings.getOverrideReason());
        p.setStatutoryVersion(rules.version()); p.setExchangeRateToVnd(fx);
        BigDecimal expected = new BigDecimal(CompanySecretKeyCryptoUtils.decrypt(c.getEmployeeSalary().getTotalAmount(), security.getCurrentCompanySecretKey()));
        p.setExpectedAmount(money(expected, currency));
        p.getHours().put("Expected hours", nz(c.getExpectedWorkingHourPerMonth()));
        p.getHours().put("Paid regular hours (including paid leave)", nz(c.getActualWorkingHourPerMonth()));
        p.getHours().put("Worked hours (excluding paid leave)", nz(c.getActualWorkingHourPerMonth()).subtract(nz(c.getPaidLeaveHours())));
        p.getHours().put("Paid leave", nz(c.getPaidLeaveHours()));
        p.getHours().put("Unpaid leave", nz(c.getUnpaidLeaveHours()));
        p.getHours().put("Late arrival / early departure", nz(c.getLateEarlyDeductionHours()));
        p.getNotes().add("Unpaid leave and late/early time already reduce payable hours. Information rows are not deducted again.");
        p.getNotes().add("Overtime and night-work tax exemptions use the statutory limit; company payments above that limit remain taxable from 2026.");
        p.getNotes().add("Monthly tax withholding; annual tax finalization is separate. Insurance eligibility and declared contribution salary come from confirmed employee setup.");
        BigDecimal hourly = c.getExpectedWorkingHourPerMonth().signum() > 0 ? expected.divide(c.getExpectedWorkingHourPerMonth(), 12, RoundingMode.HALF_UP) : ZERO;
        if (c.getSalaryBasisType() == SalaryBasisType.HOURLY) {
            hourly = c.getDetails().stream().filter(d -> d.getSalary() != null && !Boolean.TRUE.equals(d.getSalary().getIsDeduct()))
                .filter(d -> d.getSalary().getCalculateMethod() == SalaryCalculateMethod.FIXED)
                .filter(d -> d.getUnitType() != SalaryUnitType.FIXED_AMOUNT)
                .map(d -> new BigDecimal(d.getAmount())).reduce(ZERO, BigDecimal::add);
        }
        c.setStandardMoneyPerHour(hourly);
        add(p,"INFO","Reference hourly rate",c.getExpectedWorkingHourPerMonth(),null,null,hourly,
            c.getSalaryBasisType() == SalaryBasisType.HOURLY ? "Sum of configured hourly earning rates" : n(expected)+" / "+n(c.getExpectedWorkingHourPerMonth())+" expected hours", currency);
        // A fixed component is not attendance-prorated; variable monthly components are.
        Map<String, EmployeeSalaryDetail> details = new LinkedHashMap<>();
        for (EmployeeSalaryDetail d : c.getDetails()) {
            if (d.getSalary() == null || d.getSalary().getCode() == null) throw new BadRequestException("A salary component is missing its definition");
            if (details.put(d.getSalary().getCode(),d) != null) throw new BadRequestException("Duplicate salary component: "+d.getSalary().getCode());
        }
        BigDecimal payable = nz(c.getActualWorkingHourPerMonth());
        BigDecimal ratio = payable.divide(c.getExpectedWorkingHourPerMonth(),12,RoundingMode.HALF_UP);
        boolean monthly = c.getSalaryBasisType() == SalaryBasisType.MONTHLY || c.getSalaryBasisType() == SalaryBasisType.WORKING_HOUR;
        BigDecimal quantity = monthly ? ratio : c.getSalaryBasisCalculationResult().actualBasisValue();
        if (details.isEmpty()) {
            if (!monthly) throw new BadRequestException("Configure a fixed earning component for the salary unit rate");
            add(p,"EARNING","Regular earnings",payable,hourly,BigDecimal.ONE,payable.multiply(hourly),n(payable)+" hours \u00d7 "+n(hourly),currency);
        }
        Map<String,BigDecimal> values = new HashMap<>();
        for (String code : details.keySet()) component(code, details, values, new HashSet<>(), p, quantity, expected, payable, hourly, monthly);
        add(p,"INFO","Paid leave included in regular earnings",nz(c.getPaidLeaveHours()),hourly,null,ZERO,"Included in payable hours; no additional payment",currency);
        add(p,"INFO","Unpaid leave reduction",nz(c.getUnpaidLeaveHours()),hourly,null,nz(c.getUnpaidLeaveHours()).multiply(hourly),n(c.getUnpaidLeaveHours())+" hours \u00d7 "+n(hourly)+"; already excluded from payable time",currency);
        add(p,"INFO","Late / early reduction",nz(c.getLateEarlyDeductionHours()),hourly,null,nz(c.getLateEarlyDeductionHours()).multiply(hourly),n(c.getLateEarlyDeductionHours())+" hours \u00d7 "+n(hourly)+"; already excluded from payable time",currency);
        BigDecimal overtimeExemption = overtime(c,p,in,settings,rules);
        BigDecimal gross = sum(p,"EARNING");
        
        p.setGrossEarnings(gross);
        BigDecimal insuredVnd = in.getInsuranceSalary().multiply(fx);
        boolean insured = in.getSocialInsurance() || in.getHealthInsurance() || in.getUnemploymentInsurance();
        if (insured && insuredVnd.compareTo(rules.regionalMinimum()) < 0)
            throw new BadRequestException("Declared insurance salary is below the regional minimum. Confirm contribution salary and monthly eligibility.");
        BigDecimal socialBase = insuredVnd.min(rules.insuranceCap());
        BigDecimal uiBase = insuredVnd.min(rules.regionalMinimum().multiply(new BigDecimal("20")));
        BigDecimal insuranceVnd = ZERO;
        if (in.getSocialInsurance()) {
            insuranceVnd = insuranceVnd.add(contribution(p,"Social insurance",socialBase,n(or(settings.getEmployeeSocialRate(),".08")),fx));
            employer(p,"Employer social / occupational insurance",socialBase,n(or(settings.getEmployerSocialRate(),".175")),fx);
        }
        if (in.getHealthInsurance()) {
            insuranceVnd = insuranceVnd.add(contribution(p,"Health insurance",socialBase,n(or(settings.getEmployeeHealthRate(),".015")),fx));
            employer(p,"Employer health insurance",socialBase,n(or(settings.getEmployerHealthRate(),".03")),fx);
        }
        if (in.getUnemploymentInsurance()) {
            insuranceVnd = insuranceVnd.add(contribution(p,"Unemployment insurance",uiBase,n(or(settings.getEmployeeUnemploymentRate(),".01")),fx));
            employer(p,"Employer unemployment insurance",uiBase,n(or(settings.getEmployerUnemploymentRate(),".01")),fx);
        }
        BigDecimal exempt = overtimeExemption.add(in.getTaxExemptAllowances()).multiply(fx);
        BigDecimal grossVnd = gross.multiply(fx);
        if (exempt.compareTo(grossVnd) > 0) throw new BadRequestException("Tax-exempt income exceeds gross earnings; check payroll setup");
        BigDecimal relief = in.getTaxResident() ? rules.personalRelief().add(rules.dependentRelief().multiply(BigDecimal.valueOf(in.getDependents()))).add(in.getOtherTaxRelief().multiply(fx)) : ZERO;
        BigDecimal taxable = grossVnd.subtract(exempt).subtract(in.getTaxResident() ? insuranceVnd.add(relief) : ZERO).max(ZERO).setScale(0,RoundingMode.HALF_UP);
        p.setTaxableIncomeVnd(taxable);
        add(p,"INFO","Tax-exempt earnings",null,null,null,exempt,"Overtime/night exemption + declared exempt allowances", "VND");
        add(p,"INFO","Personal and dependent tax relief",null,null,null,relief,in.getTaxResident() ? n(rules.personalRelief())+" + "+in.getDependents()+" \u00d7 "+n(rules.dependentRelief())+" + "+n(in.getOtherTaxRelief().multiply(fx)) : "Non-resident: no personal/dependent relief", "VND");
        add(p,"INFO","Taxable income",null,null,null,taxable,"max(0, "+n(grossVnd)+" \u2212 "+n(exempt)+" \u2212 "+n(in.getTaxResident()?insuranceVnd:ZERO)+" \u2212 "+n(relief)+")", "VND");
        BigDecimal tax = ZERO;
        if (in.getTaxResident()) {
            for (VietnamPayrollRules.Band band : rules.taxBands()) {
                BigDecimal portion = (band.upper()==null ? taxable : taxable.min(band.upper())).subtract(band.lower()).max(ZERO);
                BigDecimal part = portion.multiply(band.rate()).setScale(0,RoundingMode.HALF_UP);
                if (portion.signum()>0) add(p,"TAX_BAND","Tax band above "+n(band.lower()),portion,band.rate(),null,part,n(portion)+" \u00d7 "+n(band.rate().multiply(new BigDecimal("100")))+"%","VND");
                tax=tax.add(part);
            }
        } else {
            tax=taxable.multiply(new BigDecimal(".20"));
            add(p,"TAX_BAND","Non-resident tax",taxable,new BigDecimal(".20"),null,tax,n(taxable)+" \u00d7 20%","VND");
        }
        tax=tax.setScale(0,RoundingMode.HALF_UP);
        add(p,"DEDUCTION","Personal income tax",null,null,null,tax.divide(fx,12,RoundingMode.HALF_UP),n(tax)+" VND / "+n(fx)+" VND per "+currency,currency);
        if (in.getOtherDeduction().signum()>0) add(p,"DEDUCTION","Other deduction",null,null,null,in.getOtherDeduction(),in.getDeductionReason(),currency);
        p.setTotalDeductions(sum(p,"DEDUCTION"));
        p.setNetPay(p.getGrossEarnings().subtract(p.getTotalDeductions()));
        if (p.getNetPay().signum()<0) throw new BadRequestException("Deductions exceed gross earnings. Resolve the payroll inputs before finalizing.");
        if (!vnd) p.getNotes().add("Conversion: 1 "+currency+" = "+n(fx)+" VND. Source: "+in.getExchangeRateSource());
        return p;
    }

    private BigDecimal component(String code, Map<String,EmployeeSalaryDetail> details, Map<String,BigDecimal> values,
            Set<String> path, MonthlyPayslipResponse p, BigDecimal ratio, BigDecimal expected, BigDecimal hours, BigDecimal hourly, boolean monthly) {
        if (values.containsKey(code)) return values.get(code);
        if (!path.add(code)) throw new BadRequestException("Circular salary component dependency: "+code);
        EmployeeSalaryDetail d=details.get(code);
        if(d==null) throw new BadRequestException("Missing salary component dependency: "+code);
        BigDecimal configured = new BigDecimal(d.getAmount());
        if(configured.signum()<0) throw new BadRequestException("Salary component amounts must be nonnegative");
        BigDecimal base=expected.multiply(ratio);
        String dependency=d.getDependenceCode()==null ? null : d.getDependenceCode().getCode();
        if(dependency!=null) base=component(dependency,details,values,path,p,ratio,expected,hours,hourly,monthly);
        SalaryCalculateMethod method=d.getSalary().getCalculateMethod();
        if(method==null) method=SalaryCalculateMethod.FIXED;
        BigDecimal amount; String formula;
        switch(method) {
            case PERCENT -> { amount=base.multiply(configured).divide(new BigDecimal("100"),12,RoundingMode.HALF_UP); formula=n(base)+" \u00d7 "+n(configured)+"%"; }
            case PLUS -> { amount=base.add(configured); formula=n(base)+" + "+n(configured); }
            case MINUS -> { amount=base.subtract(configured); formula=n(base)+" \u2212 "+n(configured); }
            case DIVIDE -> { if(configured.signum()==0) throw new BadRequestException("Salary divisor must be positive"); amount=base.divide(configured,12,RoundingMode.HALF_UP); formula=n(base)+" / "+n(configured); }
            case FORMULA -> throw new BadRequestException("Custom FORMULA component "+code+" needs a supported explicit calculation method before statutory payroll can run");
            default -> { boolean fixed=(monthly && Boolean.TRUE.equals(d.getIsFixed())) || d.getUnitType()==SalaryUnitType.FIXED_AMOUNT || Boolean.TRUE.equals(d.getSalary().getIsDeduct()); amount=fixed ? configured : configured.multiply(ratio); formula=fixed ? "Fixed amount: "+n(configured) : n(configured)+(monthly ? " \u00d7 payable/expected hours ("+n(ratio)+")" : " \u00d7 "+n(ratio)+" payable units"); }
        }
        if(amount.signum()<0) throw new BadRequestException("Negative component result: "+code);
        values.put(code,amount);path.remove(code);
        add(p,Boolean.TRUE.equals(d.getSalary().getIsDeduct()) ? "DEDUCTION" : "EARNING",d.getSalary().getName(),null,null,null,amount,formula+(dependency==null?"":"; base from "+dependency),p.getCurrency());
        return amount;
    }

    private BigDecimal overtime(MonthlySalaryCalculationContext c, MonthlyPayslipResponse p, EmployeePayrollInputs in,
            PayrollStatutorySettings s, VietnamPayrollRules rules) {
        BigDecimal exempt=ZERO,totalHours=ZERO;
        boolean fullExemption = rules.fullOvertimeExemption() && (in.getTaxResident() || !c.getMonth().isBefore(java.time.YearMonth.of(2026,7)));
        for(DailyWork work:c.getDailyWorks()) {
            DayType type=work.getWorkType()==null?work.getDayType():work.getWorkType();
            boolean holiday=type==DayType.HOLIDAY_WORK || type==DayType.HOLIDAY;
            boolean rest=type==DayType.WEEKEND_WORK || type==DayType.WEEKEND || type==DayType.COMPANY_DAY_OFF;
            BigDecimal ot=work.getOvertimeHours()==null ? BigDecimal.valueOf(work.getOtTime()==null?0:work.getOtTime()).divide(new BigDecimal("60"),12,RoundingMode.HALF_UP) : work.getOvertimeHours();
            BigDecimal night=nz(work.getNightHours()), nightOt=nz(work.getNightOvertimeHours());
            BigDecimal rate=in.getOvertimeHourlyRate();
            BigDecimal statutoryMult = new BigDecimal(holiday ? "3" : rest ? "2" : "1.5");
            BigDecimal mult=holiday?or(s.getHolidayOvertimeMultiplier(),"3"):rest?or(s.getRestDayOvertimeMultiplier(),"2"):or(s.getWeekdayOvertimeMultiplier(),"1.5");
            if(ot.signum()<0 || night.signum()<0 || nightOt.signum()<0 || nightOt.compareTo(ot)>0) throw new BadRequestException("Invalid night/overtime hours on "+work.getWorkingDate());
            BigDecimal hours=work.getHoursWorked()==null?BigDecimal.valueOf(work.getQuantity()==null?0:work.getQuantity()):work.getHoursWorked();
            if (hours.signum() < 0) throw new BadRequestException("Negative regular hours on " + work.getWorkingDate());
            boolean paidWork = type == DayType.NORMAL || type == DayType.HOLIDAY_WORK || type == DayType.WEEKEND_WORK;
            if (!paidWork) {
                if (ot.signum() > 0 || night.signum() > 0 || nightOt.signum() > 0 || ((holiday || rest) && hours.signum() > 0))
                    throw new BadRequestException("Classify worked time as NORMAL, HOLIDAY_WORK or WEEKEND_WORK before calculating payroll on " + work.getWorkingDate());
                continue;
            }
            if(night.compareTo(hours)>0) throw new BadRequestException("Night regular hours exceed regular hours on "+work.getWorkingDate());
            String day=holiday?"Holiday":rest?"Rest day":"Weekday";
            // Rest/holiday hours already included at base rate in regular earnings: add only the premium here.
            if((holiday||rest) && hours.signum()>0) {
                BigDecimal premium=rate.multiply(hours).multiply(mult.subtract(BigDecimal.ONE));
                add(p,"EARNING",day+" work premium \u00b7 "+work.getWorkingDate(),hours,rate,mult.subtract(BigDecimal.ONE),premium,n(hours)+" \u00d7 "+n(rate)+" \u00d7 ("+n(mult)+" \u2212 1); base already included",p.getCurrency());
                exempt=exempt.add(fullExemption?rate.multiply(hours).multiply(statutoryMult):premium);
            }
            BigDecimal daytime=ot.subtract(nightOt);
            if(daytime.signum()>0) {
                BigDecimal amount=rate.multiply(daytime).multiply(mult);
                add(p,"EARNING",day+" overtime \u00b7 "+work.getWorkingDate(),daytime,rate,mult,amount,n(daytime)+" \u00d7 "+n(rate)+" \u00d7 "+n(mult),p.getCurrency());
                exempt=exempt.add(fullExemption?rate.multiply(daytime).multiply(statutoryMult):rate.multiply(daytime).multiply(mult.subtract(BigDecimal.ONE)));
            }
            BigDecimal nightPremium=or(s.getNightWorkPremium(),".30");
            if(night.signum()>0) {
                BigDecimal amount=night.multiply(rate).multiply(nightPremium);
                add(p,"EARNING","Night work premium \u00b7 "+work.getWorkingDate(),night,rate,nightPremium,amount,n(night)+" \u00d7 "+n(rate)+" \u00d7 "+n(nightPremium),p.getCurrency());
                exempt=exempt.add(fullExemption ? night.multiply(rate).multiply(new BigDecimal(holiday || rest ? ".30" : "1.30")) : amount);
            }
            if(nightOt.signum()>0) {
                BigDecimal daytimeRate=(holiday||rest||daytime.signum()>0)?mult:BigDecimal.ONE;
                BigDecimal nightMult=mult.add(nightPremium).add(new BigDecimal(".20").multiply(daytimeRate));
                BigDecimal amount=nightOt.multiply(rate).multiply(nightMult);
                add(p,"EARNING",day+" night overtime \u00b7 "+work.getWorkingDate(),nightOt,rate,nightMult,amount,n(nightOt)+" \u00d7 "+n(rate)+" \u00d7 ("+n(mult)+" + "+n(nightPremium)+" + 0.20 \u00d7 "+n(daytimeRate)+")",p.getCurrency());
                BigDecimal statutoryDayRate = holiday || rest || daytime.signum()>0 ? statutoryMult : BigDecimal.ONE;
                BigDecimal exemptNightMult = statutoryMult.add(new BigDecimal(".30")).add(new BigDecimal(".20").multiply(statutoryDayRate));
                exempt=exempt.add(fullExemption?nightOt.multiply(rate).multiply(exemptNightMult):nightOt.multiply(rate).multiply(nightMult.subtract(BigDecimal.ONE)));
            }
            totalHours=totalHours.add(ot);
        }
        p.getHours().put("Overtime (including night OT)",totalHours);
        return money(exempt,p.getCurrency());
    }
    private BigDecimal contribution(MonthlyPayslipResponse p,String label,BigDecimal base,String rate,BigDecimal fx) {
        BigDecimal vnd=base.multiply(new BigDecimal(rate)).setScale(0,RoundingMode.HALF_UP);
        add(p,"DEDUCTION",label,base,new BigDecimal(rate),null,vnd.divide(fx,12,RoundingMode.HALF_UP),n(base)+" VND (capped contribution base) \u00d7 "+n(new BigDecimal(rate).multiply(new BigDecimal("100")))+"% / "+n(fx),p.getCurrency());
        return vnd;
    }
    private void employer(MonthlyPayslipResponse p,String label,BigDecimal base,String rate,BigDecimal fx) {
        BigDecimal vnd=base.multiply(new BigDecimal(rate)).setScale(0,RoundingMode.HALF_UP);
        add(p,"EMPLOYER",label,base,new BigDecimal(rate),null,vnd.divide(fx,12,RoundingMode.HALF_UP),n(base)+" VND \u00d7 "+n(new BigDecimal(rate).multiply(new BigDecimal("100")))+"% / "+n(fx)+"; not deducted from pay",p.getCurrency());
    }
    private static BigDecimal sum(MonthlyPayslipResponse p,String kind) { return p.getLines().stream().filter(l->kind.equals(l.kind())).map(Line::amount).reduce(ZERO,BigDecimal::add); }
    private static void add(MonthlyPayslipResponse p,String kind,String label,BigDecimal qty,BigDecimal rate,BigDecimal multiplier,BigDecimal amount,String formula,String currency) { p.getLines().add(new Line(kind,label,qty,rate,multiplier,money(amount,currency),formula,currency)); }
    private static BigDecimal money(BigDecimal value,String currency) { return value.setScale("VND".equalsIgnoreCase(currency)?0:2,RoundingMode.HALF_UP); }
    private static BigDecimal nz(BigDecimal value) { return value==null?ZERO:value; }
    private static BigDecimal or(BigDecimal value,String fallback) { return value==null?new BigDecimal(fallback):value; }
    private static String n(BigDecimal value) { return nz(value).stripTrailingZeros().toPlainString(); }
}
