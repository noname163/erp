package com.dat.erp.services.impl;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.salary.MonthlySalaryCalculationResponse;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.services.MonthlySalaryCalculationService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.salary.calculation.MonthlySalaryCalculationContext;
import com.dat.erp.services.salary.calculation.MonthlySalaryCalculationStep;
import com.dat.erp.services.salary.calculation.basis.SalaryBasisCalculationResult;
import com.dat.erp.utils.CustomStringUtils;

@Service
public class MonthlySalaryCalculationServiceImpl implements MonthlySalaryCalculationService {

    private final SecurityContextService securityContextService;
    private final List<MonthlySalaryCalculationStep> calculationSteps;

    public MonthlySalaryCalculationServiceImpl(SecurityContextService securityContextService,
            List<MonthlySalaryCalculationStep> calculationSteps) {
        this.securityContextService = securityContextService;
        this.calculationSteps = calculationSteps.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlySalaryCalculationResponse calculateEmployeeMonthlySalary(String employeeCode, YearMonth month) {
        String normalizedEmployeeCode = CustomStringUtils.normalizeCode(employeeCode);
        if (normalizedEmployeeCode == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_EMPLOYEE_CODE_INVALID);
        }
        if (month == null) {
            throw new BadRequestException(Messages.ERROR_PAYROLL_MONTH_INVALID);
        }

        String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
        MonthlySalaryCalculationContext context = new MonthlySalaryCalculationContext(
                normalizedEmployeeCode, companyCode, month);
        calculationSteps.forEach(step -> step.execute(context));
        SalaryBasisCalculationResult basisResult = context.getSalaryBasisCalculationResult();

        return new MonthlySalaryCalculationResponse(context.getEmployeeCode(), context.getMonth(),
                context.getExpectedWorkingHourPerMonth(), context.getActualWorkingHourPerMonth(),
                context.getStandardMoneyPerHour(), context.getFinalSalary(),
                Collections.unmodifiableMap(context.getActualHoursByDayType()), List.copyOf(context.getAuditTrail()),
                basisResult.salaryBasisType(), basisResult.basisUnit(), basisResult.expectedBasisValue(),
                basisResult.actualBasisValue(), basisResult.standardMoneyPerUnit(),
                Collections.unmodifiableMap(basisResult.actualBasisValuesByType()));
    }
}
