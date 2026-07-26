package com.dat.erp.services.payroll.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dat.erp.constants.PayrollRunAuditActionType;
import com.dat.erp.constants.PayrollRunAuditStatus;
import com.dat.erp.dto.request.PayrollRunAuditLogRequest;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.entities.PayrollResultSnapshot;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.repositories.customrepositories.PayrollResultSnapshotRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollResultSnapshotService;
import com.dat.erp.services.payroll.PayrollRunAuditLogService;
import com.dat.erp.utils.CustomStringUtils;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PayrollResultSnapshotServiceImpl implements PayrollResultSnapshotService {

    private final PayrollResultSnapshotRepository payrollResultSnapshotRepository;
    private final PayrollRunAuditLogService payrollRunAuditLogService;
    private final SecurityContextService securityContextService;
    private final PayrollResultDetailRepository payrollResultDetailRepository;

    @Override
    public void createSnapshot(PayrollRun payrollRun, PayrollResult oldResult, String rerunBatchCode,
            String employeeCode) {
        PayrollResultSnapshot snapshot = new PayrollResultSnapshot();
        snapshot.setPayrollRunCode(payrollRun.getCode());
        snapshot.setPayrollResultCode(oldResult.getCode());
        snapshot.setRerunBatchCode(rerunBatchCode);
        snapshot.setEmployeeCode(employeeCode);
        snapshot.setExpectedAmount(oldResult.getExpectedAmount());
        snapshot.setActualAmount(oldResult.getActualAmount());
        snapshot.setExpectedQuantity(oldResult.getExpectedQuantity());
        snapshot.setActualQuantity(oldResult.getActualQuantity());
        snapshot.setCurrency(oldResult.getCurrency());
        snapshot.setSourceType(oldResult.getSourceType());
        snapshot.setResultJson(payrollResultService.toResultJson(oldResult));
        snapshot.setDetailJson(toDetailJson(oldResult));
        snapshot.setEmployeeSalaryCode(
                oldResult.getEmployeeSalary() == null ? null : oldResult.getEmployeeSalary().getCode());
        snapshot.setSnapshotAt(LocalDateTime.now(ZoneOffset.UTC));
        snapshot.setSnapshotBy(securityContextService.getCurrentUserCode());
        payrollResultSnapshotRepository.save(snapshot);
        payrollRunAuditLogService.writeAudit(
                payrollRun,
                new PayrollRunAuditLogRequest(rerunBatchCode,
                        PayrollRunAuditActionType.OLD_RESULT_SNAPSHOT_CREATED, null, employeeCode, oldResult, null,
                        PayrollRunAuditStatus.SUCCESS, null));
    }

    private String toDetailJson(PayrollResult payrollResult) {
        List<PayrollResultDetail> details = payrollResultDetailRepository
                .findByPayrollResult_CodeAndIsDeletedFalse(payrollResult.getCode());
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < details.size(); i++) {
            PayrollResultDetail detail = details.get(i);
            if (i > 0) {
                json.append(",");
            }
            json.append("{")
                    .append("\"code\":\"").append(CustomStringUtils.escapeJson(detail.getCode())).append("\",")
                    .append("\"calcBasis\":\"").append(detail.getCalcBasis()).append("\",")
                    .append("\"basisHours\":").append(detail.getBasisHours()).append(",")
                    .append("\"amount\":").append(detail.getAmount()).append(",")
                    .append("\"formulaNote\":\"").append(CustomStringUtils.escapeJson(detail.getFormulaNote()))
                    .append("\"")
                    .append("}");
        }
        json.append("]");
        return json.toString();
    }

}
