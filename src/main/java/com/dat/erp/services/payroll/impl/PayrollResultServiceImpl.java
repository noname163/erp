package com.dat.erp.services.payroll.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.DayType;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.dto.response.salary.DailyWorkForSalaryResponse;
import com.dat.erp.entities.DailyWork;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.exceptions.BadRequestException;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.CalendarDateService;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.EmployeePayrollPolicyService;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.UserProfileService;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.services.payroll.PayrollResultService;

@Service
public class PayrollResultServiceImpl extends AbstractAuditableService implements PayrollResultService {

    private static final Set<DayType> WORKING_DAY_TYPES = EnumSet.of(
            DayType.NORMAL,
            DayType.HOLIDAY_WORK,
            DayType.WEEKEND_WORK);

    private static final Set<DayType> LEAVE_DAY_TYPES = EnumSet.of(
            DayType.PTO_PAID,
            DayType.PTO_UNPAID,
            DayType.UNPAID_LEAVE,
            DayType.COMPANY_DAY_OFF);

    private final UserProfileService userProfileService;
    private final EmployeePayrollPolicyService employeePayrollPolicyService;
    private final CalendarDateService calendarDateService;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final DailyWorkRepository dailyWorkRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayrollResultRepository payrollResultRepository;

    public PayrollResultServiceImpl(
            UserProfileService userProfileService,
            EmployeePayrollPolicyService employeePayrollPolicyService,
            CalendarDateService calendarDateService,
            EmployeeSalaryRepository employeeSalaryRepository,
            DailyWorkRepository dailyWorkRepository,
            PayrollRunRepository payrollRunRepository,
            PayrollResultRepository payrollResultRepository,
            CodeGenerator codeGenerator,
            SecurityContextService securityContextService) {
        this.userProfileService = userProfileService;
        this.employeePayrollPolicyService = employeePayrollPolicyService;
        this.calendarDateService = calendarDateService;
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.dailyWorkRepository = dailyWorkRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payrollResultRepository = payrollResultRepository;
        this.codeGenerator = codeGenerator;
        this.securityContextService = securityContextService;
    }

    @Override
    @Transactional
    public void generatePayrollResult(PayrollRun payrollRun) {
        String companyCode = resolveCurrentUserCompanyCode();
        if (companyCode == null || companyCode.isBlank() || "SYSTEM".equals(companyCode)) {
            throw new BadRequestException(Messages.ERROR_CURRENT_USER_COMPANY_MISSING);
        }

        LocalDate runDate = LocalDate.now();
        YearMonth runMonth = YearMonth.from(runDate);
        List<String> activeEmployeeCodes = userProfileService.getActiveUserProfileCodesOfCurrentCompany();
        if (activeEmployeeCodes == null || activeEmployeeCodes.isEmpty()) {
            return;
        }

        Map<String, PayrollPolicy> payrollPoliciesByEmployeeCode = employeePayrollPolicyService
                .getCompanyPoliciesByEmployeeCodesAndDate(activeEmployeeCodes, runDate);
        List<EmployeeSalary> activeEmployeeSalaries = employeeSalaryRepository
                .findActiveByCompanyCodeAndUserProfileCodesAndDate(companyCode, activeEmployeeCodes, runDate);

        int totalWorkingDays = calculateTotalWorkingDays(
                calendarDateService.getCalendarDateTotalsByCompanyCodeAndMonth(companyCode, runMonth));
        Map<String, Integer> leaveQuantitiesByEmployeeCode = loadLeaveQuantitiesByEmployeeCode(
                companyCode,
                activeEmployeeCodes,
                runMonth);
        Map<String, EmployeeSalary> salaryByEmployeeCode = mapSalariesByEmployeeCode(activeEmployeeSalaries);

        List<PayrollResult> payrollResults = new ArrayList<>();
        for (String employeeCode : activeEmployeeCodes) {
            EmployeeSalary employeeSalary = salaryByEmployeeCode.get(employeeCode);
            PayrollPolicy payrollPolicy = payrollPoliciesByEmployeeCode.get(employeeCode);
            if (employeeSalary == null || payrollPolicy == null) {
                continue;
            }

            payrollResults.add(buildPayrollResult(
                    payrollRun,
                    employeeSalary,
                    payrollPolicy,
                    totalWorkingDays,
                    leaveQuantitiesByEmployeeCode.getOrDefault(employeeCode, 0)));
        }

        if (!payrollResults.isEmpty()) {
            payrollResultRepository.saveAll(payrollResults);
        }

        payrollRun.setStatus(PayrollRunStatus.CALCULATED);
        payrollRun.setRunAt(LocalDateTime.now(ZoneOffset.UTC));
        applyUpdateAudit(payrollRun);
        payrollRunRepository.save(payrollRun);
    }

    private int calculateTotalWorkingDays(Map<DayType, Integer> totalsByDayType) {
        if (totalsByDayType == null || totalsByDayType.isEmpty()) {
            return 0;
        }

        int totalWorkingDays = 0;
        for (Map.Entry<DayType, Integer> entry : totalsByDayType.entrySet()) {
            if (WORKING_DAY_TYPES.contains(entry.getKey())) {
                totalWorkingDays += entry.getValue() == null ? 0 : entry.getValue();
            }
        }
        return totalWorkingDays;
    }

    private Map<String, Integer> loadLeaveQuantitiesByEmployeeCode(
            String companyCode,
            List<String> employeeCodes,
            YearMonth runMonth) {
        LocalDate fromDate = runMonth.atDay(1);
        LocalDate toDate = runMonth.atEndOfMonth();
        List<DailyWork> dailyWorks = dailyWorkRepository.findAllForSalaryByCompanyCodeAndEmployeeCodesAndWorkingDateBetween(
                companyCode,
                employeeCodes,
                fromDate,
                toDate);

        Map<String, Integer> leaveQuantitiesByEmployeeCode = new LinkedHashMap<>();
        for (DailyWork dailyWork : dailyWorks) {
            UserProfile userProfile = dailyWork.getUserProfile();
            if (userProfile == null || userProfile.getCode() == null || dailyWork.getWorkType() == null) {
                continue;
            }
            if (!LEAVE_DAY_TYPES.contains(dailyWork.getWorkType())) {
                continue;
            }

            leaveQuantitiesByEmployeeCode.merge(
                    userProfile.getCode(),
                    toDailyWorkQuantity(dailyWork),
                    Integer::sum);
        }
        return leaveQuantitiesByEmployeeCode;
    }

    private Map<String, EmployeeSalary> mapSalariesByEmployeeCode(List<EmployeeSalary> employeeSalaries) {
        Map<String, EmployeeSalary> salaryByEmployeeCode = new LinkedHashMap<>();
        for (EmployeeSalary employeeSalary : employeeSalaries) {
            UserProfile userProfile = employeeSalary.getUserProfile();
            if (userProfile == null || userProfile.getCode() == null) {
                continue;
            }
            salaryByEmployeeCode.putIfAbsent(userProfile.getCode(), employeeSalary);
        }
        return salaryByEmployeeCode;
    }

    private PayrollResult buildPayrollResult(
            PayrollRun payrollRun,
            EmployeeSalary employeeSalary,
            PayrollPolicy payrollPolicy,
            int totalWorkingDays,
            int leaveQuantity) {
        int standardQuantityPerDay = payrollPolicy.getStandardQuantityPerDay() == null
                ? 0
                : payrollPolicy.getStandardQuantityPerDay();
        int expectedQuantity = standardQuantityPerDay * totalWorkingDays;
        int actualQuantity = Math.max(expectedQuantity - leaveQuantity, 0);

        PayrollResult payrollResult = PayrollResult.builder()
                .payrollRun(payrollRun)
                .employeeSalary(employeeSalary)
                .amount(employeeSalary.getTotalAmount())
                .currency(employeeSalary.getCurrency())
                .expectedQuantity(expectedQuantity)
                .actualQuantity(actualQuantity)
                .unit(payrollPolicy.getUnit())
                .sourceType(PayrollStatus.PREVIEW)
                .isRetro(false)
                .retroReason(null)
                .build();
        generateCodeIfMissing(payrollResult, CodePrefixes.PAYROLL_RESULT);
        applyInsertAudit(payrollResult);
        return payrollResult;
    }

    private int toDailyWorkQuantity(DailyWork dailyWork) {
        return new DailyWorkForSalaryResponse(
                dailyWork.getWorkType(),
                dailyWork.getHoursWorked() == null ? 0 : dailyWork.getHoursWorked().intValue())
                .getTotalWorkHours();
    }
}
