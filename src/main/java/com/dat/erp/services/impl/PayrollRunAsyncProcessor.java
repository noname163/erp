package com.dat.erp.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.CodePrefixes;
import com.dat.erp.constants.Messages;
import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.entities.BaseAuditableEntity;
import com.dat.erp.entities.CompanyCalendar;
import com.dat.erp.entities.EmployeeSalary;
import com.dat.erp.entities.PayRateRule;
import com.dat.erp.entities.PayrollEmployeeSummary;
import com.dat.erp.entities.PayrollPolicy;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.entities.UserProfile;
import com.dat.erp.entities.WorkSchedule;
import com.dat.erp.entities.WorkScheduleDetail;
import com.dat.erp.repositories.customrepositories.CalendarDateRepository;
import com.dat.erp.repositories.customrepositories.CompanyCalendarRepository;
import com.dat.erp.repositories.customrepositories.DailyWorkRepository;
import com.dat.erp.repositories.customrepositories.EmployeePtoRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryDetailRepository;
import com.dat.erp.repositories.customrepositories.EmployeeSalaryRepository;
import com.dat.erp.repositories.customrepositories.EmployeeScheduleAssignmentRepository;
import com.dat.erp.repositories.customrepositories.EmploymentAgreementRepository;
import com.dat.erp.repositories.customrepositories.PayRateRuleRepository;
import com.dat.erp.repositories.customrepositories.PayrollAdjustmentRepository;
import com.dat.erp.repositories.customrepositories.PayrollEmployeeSummaryRepository;
import com.dat.erp.repositories.customrepositories.PayrollPolicyRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.repositories.customrepositories.UserProfileRepository;
import com.dat.erp.repositories.customrepositories.WorkScheduleDetailRepository;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.PayrollCalculationService;
import com.dat.erp.services.PayrollEmployeeDraft;
import com.dat.erp.services.PayrollLineDraft;

@Service
public class PayrollRunAsyncProcessor {

    private final PayrollRunRepository payrollRunRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmploymentAgreementRepository employmentAgreementRepository;
    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final EmployeeSalaryDetailRepository employeeSalaryDetailRepository;
    private final PayrollPolicyRepository payrollPolicyRepository;
    private final PayRateRuleRepository payRateRuleRepository;
    private final EmployeeScheduleAssignmentRepository employeeScheduleAssignmentRepository;
    private final WorkScheduleDetailRepository workScheduleDetailRepository;
    private final CompanyCalendarRepository companyCalendarRepository;
    private final CalendarDateRepository calendarDateRepository;
    private final DailyWorkRepository dailyWorkRepository;
    private final EmployeePtoRepository employeePtoRepository;
    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final PayrollEmployeeSummaryRepository payrollEmployeeSummaryRepository;
    private final PayrollResultRepository payrollResultRepository;
    private final PayrollResultDetailRepository payrollResultDetailRepository;
    private final PayrollCalculationService payrollCalculationService;
    private final CodeGenerator codeGenerator;

    public PayrollRunAsyncProcessor(
            PayrollRunRepository payrollRunRepository,
            UserProfileRepository userProfileRepository,
            EmploymentAgreementRepository employmentAgreementRepository,
            EmployeeSalaryRepository employeeSalaryRepository,
            EmployeeSalaryDetailRepository employeeSalaryDetailRepository,
            PayrollPolicyRepository payrollPolicyRepository,
            PayRateRuleRepository payRateRuleRepository,
            EmployeeScheduleAssignmentRepository employeeScheduleAssignmentRepository,
            WorkScheduleDetailRepository workScheduleDetailRepository,
            CompanyCalendarRepository companyCalendarRepository,
            CalendarDateRepository calendarDateRepository,
            DailyWorkRepository dailyWorkRepository,
            EmployeePtoRepository employeePtoRepository,
            PayrollAdjustmentRepository payrollAdjustmentRepository,
            PayrollEmployeeSummaryRepository payrollEmployeeSummaryRepository,
            PayrollResultRepository payrollResultRepository,
            PayrollResultDetailRepository payrollResultDetailRepository,
            PayrollCalculationService payrollCalculationService,
            CodeGenerator codeGenerator) {
        this.payrollRunRepository = payrollRunRepository;
        this.userProfileRepository = userProfileRepository;
        this.employmentAgreementRepository = employmentAgreementRepository;
        this.employeeSalaryRepository = employeeSalaryRepository;
        this.employeeSalaryDetailRepository = employeeSalaryDetailRepository;
        this.payrollPolicyRepository = payrollPolicyRepository;
        this.payRateRuleRepository = payRateRuleRepository;
        this.employeeScheduleAssignmentRepository = employeeScheduleAssignmentRepository;
        this.workScheduleDetailRepository = workScheduleDetailRepository;
        this.companyCalendarRepository = companyCalendarRepository;
        this.calendarDateRepository = calendarDateRepository;
        this.dailyWorkRepository = dailyWorkRepository;
        this.employeePtoRepository = employeePtoRepository;
        this.payrollAdjustmentRepository = payrollAdjustmentRepository;
        this.payrollEmployeeSummaryRepository = payrollEmployeeSummaryRepository;
        this.payrollResultRepository = payrollResultRepository;
        this.payrollResultDetailRepository = payrollResultDetailRepository;
        this.payrollCalculationService = payrollCalculationService;
        this.codeGenerator = codeGenerator;
    }

    @Async("payrollTaskExecutor")
    public void processPreviewAsync(String runCode, List<String> requestedEmployeeCodes) {
        processPreview(runCode, requestedEmployeeCodes);
    }

    @Transactional
    public void processPreview(String runCode, List<String> requestedEmployeeCodes) {
        PayrollRun run = payrollRunRepository.findByCodeAndIsDeletedFalse(runCode)
                .orElseThrow(() -> new IllegalArgumentException(Messages.ERROR_PAYROLL_RUN_NOT_FOUND));
        run.setStatus(PayrollRunStatus.RUNNING);
        run.setRunAt(LocalDateTime.now());
        payrollRunRepository.save(run);

        List<UserProfile> employees = resolveEmployees(run.getCompanyCode(), requestedEmployeeCodes);
        List<PayrollPolicy> policies = payrollPolicyRepository.findOverlappingPolicies(run.getCompanyCode(), run.getPeriodStart(),
                run.getPeriodEnd());
        List<PayRateRule> payRateRules = payRateRuleRepository.findByPolicy_CodeInAndIsDeletedFalseOrderByPriorityAsc(
                policies.stream().map(PayrollPolicy::getCode).toList());
        CompanyCalendar calendar = companyCalendarRepository.findOverlappingCalendars(run.getCompanyCode(), run.getPeriodStart(),
                run.getPeriodEnd()).stream().findFirst().orElse(null);
        var calendarDates = calendar == null ? List.<com.dat.erp.entities.CalendarDate>of()
                : calendarDateRepository.findByCalendar_CodeAndCalDateBetweenAndIsDeletedFalse(calendar.getCode(),
                        run.getPeriodStart(), run.getPeriodEnd());
        int blockingErrors = 0;

        for (UserProfile employee : employees) {
            String payrollMonth = YearMonth.from(run.getPeriodStart()).toString();
            var agreements = employmentAgreementRepository.findOverlappingAgreements(run.getCompanyCode(), employee.getCode(),
                    run.getPeriodStart(), run.getPeriodEnd());
            var salaryRecords = employeeSalaryRepository.findOverlappingForPayroll(run.getCompanyCode(), employee.getCode(),
                    run.getPeriodStart(), run.getPeriodEnd());
            List<String> salaryCodes = salaryRecords.stream().map(EmployeeSalary::getCode).toList();
            var salaryDetails = salaryCodes.isEmpty() ? List.<com.dat.erp.entities.EmployeeSalaryDetail>of()
                    : employeeSalaryDetailRepository.findByEmployeeSalaryCodes(salaryCodes);
            var scheduleAssignments = employeeScheduleAssignmentRepository.findApplicableAssignments(run.getCompanyCode(),
                    employee.getCode(), employee.getDepartment() == null ? null : employee.getDepartment().getCode(),
                    run.getPeriodStart(), run.getPeriodEnd());
            WorkSchedule workSchedule = scheduleAssignments.isEmpty() ? null : scheduleAssignments.get(0).getWorkSchedule();
            List<WorkScheduleDetail> scheduleDetails = workSchedule == null ? List.of()
                    : workScheduleDetailRepository.findByWorkSchedule_CodeAndIsDeletedFalseOrderByDayOfWeekAsc(
                            workSchedule.getCode());
            PayrollEmployeeDraft draft = payrollCalculationService.calculate(
                    employee,
                    run.getPeriodStart(),
                    run.getPeriodEnd(),
                    agreements,
                    salaryRecords,
                    salaryDetails,
                    policies,
                    payRateRules,
                    workSchedule,
                    scheduleDetails,
                    calendar,
                    calendarDates,
                    dailyWorkRepository.findByCompanyCodeAndUserProfile_CodeAndWorkingDateBetweenAndIsDeletedFalse(
                            run.getCompanyCode(), employee.getCode(), run.getPeriodStart(), run.getPeriodEnd()),
                    employeePtoRepository.findOverlappingPto(run.getCompanyCode(), employee.getCode(), run.getPeriodStart(),
                            run.getPeriodEnd()),
                    payrollAdjustmentRepository.findByCompanyCodeAndUserProfile_CodeAndEffectivePayrollMonthAndIsDeletedFalse(
                            run.getCompanyCode(), employee.getCode(), payrollMonth));
            persistDraft(run, draft);
            if (draft.hasBlockingIssue()) {
                blockingErrors++;
            }
        }

        run.setErrorCount(blockingErrors);
        run.setWarningCount(0);
        run.setStatus(PayrollRunStatus.PREVIEW_READY);
        payrollRunRepository.save(run);
    }

    private List<UserProfile> resolveEmployees(String companyCode, List<String> requestedEmployeeCodes) {
        List<UserProfile> all = userProfileRepository.findByCompanyCodeAndIsDeletedFalse(companyCode);
        if (requestedEmployeeCodes == null || requestedEmployeeCodes.isEmpty()) {
            return all;
        }
        Map<String, UserProfile> byCode = all.stream().collect(Collectors.toMap(UserProfile::getCode, user -> user));
        return requestedEmployeeCodes.stream().map(byCode::get).filter(Objects::nonNull).toList();
    }

    private void persistDraft(PayrollRun run, PayrollEmployeeDraft draft) {
        PayrollEmployeeSummary summary = PayrollEmployeeSummary.builder()
                .payrollRun(run)
                .userProfile(draft.userProfile())
                .grossAmount(formatAmount(draft.grossAmount()))
                .deductionAmount(formatAmount(draft.deductionAmount()))
                .netAmount(formatAmount(draft.netAmount()))
                .currency(draft.currency())
                .status(draft.status())
                .hasBlockingIssue(draft.hasBlockingIssue())
                .issueMessage(draft.issueMessage())
                .policySnapshotVersion(run.getSnapshotVersion())
                .isFrozen(false)
                .build();
        assignCodeAndAudit(summary, CodePrefixes.PAYROLL_EMPLOYEE_SUMMARY, run);
        PayrollEmployeeSummary savedSummary = payrollEmployeeSummaryRepository.save(summary);

        List<PayrollResult> results = new ArrayList<>();
        int sequence = 1;
        for (PayrollLineDraft line : draft.lines()) {
            PayrollResult result = PayrollResult.builder()
                    .payrollRun(run)
                    .employeeSummary(savedSummary)
                    .userProfile(draft.userProfile())
                    .salary(line.salary())
                    .amount(formatAmount(line.amount()))
                    .quantity(line.quantity() == null ? null : line.quantity().intValue())
                    .quantityValue(line.quantity())
                    .lineType(line.lineType())
                    .sourceType(line.sourceType())
                    .segmentFrom(line.segmentFrom())
                    .segmentTo(line.segmentTo())
                    .currency(line.currency())
                    .rate(line.rate() == null ? null : line.rate().setScale(2, RoundingMode.HALF_UP).toPlainString())
                    .multiplier(line.multiplier())
                    .sequenceOrder(sequence++)
                    .sourceRefCode(line.sourceRefCode())
                    .policySnapshotVersion(run.getSnapshotVersion())
                    .isManual(line.manual())
                    .isFrozen(false)
                    .isRetro(line.retro())
                    .retroReason(line.retroReason())
                    .build();
            assignCodeAndAudit(result, CodePrefixes.PAYROLL_RESULT, run);
            results.add(result);
        }
        List<PayrollResult> savedResults = payrollResultRepository.saveAll(results);

        List<PayrollResultDetail> details = new ArrayList<>();
        for (int index = 0; index < savedResults.size(); index++) {
            PayrollLineDraft line = draft.lines().get(index);
            PayrollResultDetail detail = PayrollResultDetail.builder()
                    .payrollResult(savedResults.get(index))
                    .calcBasis(line.calcBasis())
                    .payableQuantity(line.quantity())
                    .ratePerDay(line.rate())
                    .multiplierApplied(line.multiplier())
                    .formulaNote(line.formulaNote())
                    .policyRuleCode(line.policyRuleCode())
                    .sourceDate(line.sourceDate())
                    .build();
            assignCodeAndAudit(detail, CodePrefixes.PAYROLL_RESULT_DETAIL, run);
            details.add(detail);
        }
        payrollResultDetailRepository.saveAll(details);
    }

    private void assignCodeAndAudit(BaseAuditableEntity entity, String prefix, PayrollRun run) {
        entity.setCode(codeGenerator.nextCode(prefix));
        entity.setCompanyCode(run.getCompanyCode());
        entity.setCreatedBy(run.getCreatedBy());
        entity.setUpdatedBy(run.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setIsDeleted(false);
    }

    private String formatAmount(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
