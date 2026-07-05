package com.dat.erp.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.dat.erp.constants.PayrollRunStatus;

import lombok.Data;

@Data
public class PayrollRerunResponse {
    private String payrollRunCode;
    private String rerunBatchCode;
    private PayrollRunStatus status;
    private int totalEmployees;
    private int successCount;
    private int failedCount;
    private boolean dryRun;
    private List<PayrollRerunEmployeeResultResponse> results = new ArrayList<>();
    private List<PayrollRerunErrorResponse> errors = new ArrayList<>();
}
