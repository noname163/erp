package com.dat.erp.services.payroll.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.dat.erp.entities.PayrollRun;
import com.dat.erp.repositories.customrepositories.PayrollRunRepository;
import com.dat.erp.services.base.AbstractAuditableService;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.services.payroll.PayrollRunService;

@Service
public class PayrollRunServiceImpl extends AbstractAuditableService implements PayrollRunService {

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollResultService payrollResultService;

    public PayrollRunServiceImpl(PayrollRunRepository payrollRunRepository, PayrollResultService payrollResultService) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollResultService = payrollResultService;
    }

    @Override
    public void runPayroll() {
        PayrollRun payrollRun = new PayrollRun();
        payrollRun.setCompanyCode(resolveCurrentUserCompanyCode());
        payrollRun.setCreatedAt(LocalDateTime.now());

        applyInsertAudit(payrollRun);
        payrollRunRepository.save(payrollRun);
        payrollResultService.generatePayrollResult(payrollRun);
    }
    
}
