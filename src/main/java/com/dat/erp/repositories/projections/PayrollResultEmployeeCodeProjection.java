package com.dat.erp.repositories.projections;

import com.dat.erp.entities.PayrollResult;

public record PayrollResultEmployeeCodeProjection(
        PayrollResult payrollResult,
        String payrollResultCode,
        String employeeCode) {
}
