package com.dat.erp.services.payroll.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;
import com.dat.erp.services.payroll.PayrollResultDetailService;

@Service
public class PayrollResultDetailServiceImpl implements PayrollResultDetailService {

    private static final Logger log = LoggerFactory.getLogger(PayrollResultDetailServiceImpl.class);

    private final PayrollResultDetailRepository payrollResultDetailRepository;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public PayrollResultDetailServiceImpl(
            PayrollResultDetailRepository payrollResultDetailRepository,
            PlatformTransactionManager transactionManager) {
        this.payrollResultDetailRepository = payrollResultDetailRepository;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void calculatePayrollDetails() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePayrollDetails'");
    }

    @Override
    public void replacePayrollResultDetailsBestEffort(
            String operation,
            PayrollResult payrollResult,
            List<PayrollResultDetail> newDetails) {
        String payrollResultCode = payrollResult == null ? null : payrollResult.getCode();
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            List<PayrollResultDetail> detailsSnapshot = newDetails == null ? null : new ArrayList<>(newDetails);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    replacePayrollResultDetailsNow(operation, payrollResultCode, detailsSnapshot);
                }
            });
            log.info("PAYROLL_RESULT_DETAIL action=REPLACE_SCHEDULED operation={} payrollResultCode={} detailCount={}",
                    operation, payrollResultCode, newDetails == null ? 0 : newDetails.size());
            return;
        }

        replacePayrollResultDetailsNow(operation, payrollResultCode, newDetails);
    }

    private void replacePayrollResultDetailsNow(
            String operation,
            String payrollResultCode,
            List<PayrollResultDetail> newDetails) {
        int deletedCount = softDeleteExistingDetailsBestEffort(operation, payrollResultCode);
        int savedCount = 0;
        int failedCount = 0;

        if (newDetails != null) {
            for (PayrollResultDetail detail : newDetails) {
                if (saveDetailBestEffort(operation, payrollResultCode, detail)) {
                    savedCount++;
                } else {
                    failedCount++;
                }
            }
        }

        log.info("PAYROLL_RESULT_DETAIL action=REPLACE_FINISHED operation={} payrollResultCode={} deletedCount={} savedCount={} failedCount={}",
                operation, payrollResultCode, deletedCount, savedCount, failedCount);
    }

    private int softDeleteExistingDetailsBestEffort(String operation, String payrollResultCode) {
        if (payrollResultCode == null || payrollResultCode.isBlank()) {
            return 0;
        }

        List<PayrollResultDetail> existingDetails;
        try {
            existingDetails = payrollResultDetailRepository.findByPayrollResult_CodeAndIsDeletedFalse(payrollResultCode);
        } catch (Exception ex) {
            log.warn("PAYROLL_RESULT_DETAIL action=LOAD_EXISTING_FAILED operation={} payrollResultCode={} error={}",
                    operation, payrollResultCode, ex.getMessage(), ex);
            return 0;
        }

        int deletedCount = 0;
        for (PayrollResultDetail detail : existingDetails) {
            try {
                requiresNewTransactionTemplate.executeWithoutResult(status -> {
                    detail.setIsDeleted(true);
                    payrollResultDetailRepository.saveAndFlush(detail);
                });
                deletedCount++;
            } catch (Exception ex) {
                log.warn("PAYROLL_RESULT_DETAIL action=SOFT_DELETE_FAILED operation={} payrollResultCode={} detailCode={} calcBasis={} amount={} formulaNote={} error={}",
                        operation,
                        payrollResultCode,
                        detail.getCode(),
                        detail.getCalcBasis(),
                        detail.getAmount(),
                        detail.getFormulaNote(),
                        ex.getMessage(),
                        ex);
            }
        }
        return deletedCount;
    }

    private boolean saveDetailBestEffort(String operation, String payrollResultCode, PayrollResultDetail detail) {
        try {
            requiresNewTransactionTemplate.executeWithoutResult(status -> payrollResultDetailRepository.saveAndFlush(detail));
            log.info("PAYROLL_RESULT_DETAIL action=SAVE_SUCCESS operation={} payrollResultCode={} detailCode={} calcBasis={} amount={} formulaNote={}",
                    operation,
                    payrollResultCode,
                    detail.getCode(),
                    detail.getCalcBasis(),
                    detail.getAmount(),
                    detail.getFormulaNote());
            return true;
        } catch (Exception ex) {
            log.warn("PAYROLL_RESULT_DETAIL action=SAVE_FAILED operation={} payrollResultCode={} detailCode={} calcBasis={} basisDays={} paidDays={} unpaidDays={} basisHours={} ratePerDay={} amount={} multiplierApplied={} formulaNote={} error={}",
                    operation,
                    payrollResultCode,
                    detail == null ? null : detail.getCode(),
                    detail == null ? null : detail.getCalcBasis(),
                    detail == null ? null : detail.getBasisDays(),
                    detail == null ? null : detail.getPaidDays(),
                    detail == null ? null : detail.getUnpaidDays(),
                    detail == null ? null : detail.getBasisHours(),
                    detail == null ? null : detail.getRatePerDay(),
                    detail == null ? null : detail.getAmount(),
                    detail == null ? null : detail.getMultiplierApplied(),
                    detail == null ? null : detail.getFormulaNote(),
                    ex.getMessage(),
                    ex);
            return false;
        }
    }
}
