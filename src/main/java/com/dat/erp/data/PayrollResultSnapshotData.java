package com.dat.erp.data;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollRun;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayrollResultSnapshotData {

    private final String payrollRunCode;
    private final String payrollResultCode;
    private final String rerunBatchCode;
    private final String employeeSalaryCode;
    private final String employeeCode;
    private final String expectedAmount;
    private final String actualAmount;
    private final Integer expectedQuantity;
    private final Integer actualQuantity;
    private final String currency;
    private final PayrollStatus sourceType;
    private final String resultJson;
    private final String detailJson;
    private final LocalDateTime snapshotAt;
    private final String snapshotBy;

    public static PayrollResultSnapshotDataBuilder builder(
            PayrollRun payrollRun,
            PayrollResult oldResult,
            String rerunBatchCode,
            String employeeCode) {

        return new PayrollResultSnapshotDataBuilder()
                .payrollRunCode(payrollRun.getCode())
                .payrollResultCode(oldResult.getCode())
                .rerunBatchCode(rerunBatchCode)
                .employeeSalaryCode(Optional.ofNullable(oldResult.getEmployeeSalary())
                        .map(employeeSalary -> employeeSalary.getCode())
                        .orElse(null))
                .employeeCode(employeeCode)
                .expectedAmount(oldResult.getExpectedAmount())
                .actualAmount(oldResult.getActualAmount())
                .expectedQuantity(oldResult.getExpectedQuantity())
                .actualQuantity(oldResult.getActualQuantity())
                .currency(oldResult.getCurrency())
                .sourceType(oldResult.getSourceType())
                .snapshotAt(LocalDateTime.now(ZoneOffset.UTC));
    }
}
