package com.dat.erp.services.payroll.impl;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.dat.erp.constants.PayrollRunAuditActionType;
import com.dat.erp.constants.PayrollRunAuditStatus;
import com.dat.erp.data.PayrollResultSnapshotData;
import com.dat.erp.dto.request.PayrollRunAuditLogRequest;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultSnapshot;
import com.dat.erp.entities.PayrollRun;
import com.dat.erp.repositories.customrepositories.PayrollResultSnapshotRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollResultDetailService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.services.payroll.PayrollResultSnapshotService;
import com.dat.erp.services.payroll.PayrollRunAuditLogService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PayrollResultSnapshotServiceImpl implements PayrollResultSnapshotService {

    private final PayrollResultSnapshotRepository payrollResultSnapshotRepository;
    private final PayrollRunAuditLogService payrollRunAuditLogService;
    private final SecurityContextService securityContextService;
    private final PayrollResultDetailService payrollResultDetailService;
    private final PayrollResultService payrollResultService;

    @Override
    public void createSnapshot(PayrollRun payrollRun, PayrollResult oldResult, String rerunBatchCode,
            String employeeCode) {
        PayrollResultSnapshotData snapshotData = PayrollResultSnapshotData
                .builder(payrollRun, oldResult, rerunBatchCode, employeeCode)
                .resultJson(payrollResultService.toResultJson(oldResult))
                .detailJson(
                        payrollResultDetailService.toDetailJson(oldResult.getCode()))
                .snapshotBy(securityContextService.getCurrentUserCode())
                .build();
        PayrollResultSnapshot snapshot = Objects.requireNonNull(
                PayrollResultSnapshot.create(snapshotData));

        payrollResultSnapshotRepository.save(snapshot);
        payrollRunAuditLogService.writeAudit(
                payrollRun,
                new PayrollRunAuditLogRequest(rerunBatchCode,
                        PayrollRunAuditActionType.OLD_RESULT_SNAPSHOT_CREATED, null, employeeCode, oldResult, null,
                        PayrollRunAuditStatus.SUCCESS, null));
    }


}
