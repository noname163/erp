package com.dat.erp.services.payroll.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dat.erp.constants.PayrollResultCalcBasis;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.entities.PayrollResultDetail;
import com.dat.erp.repositories.customrepositories.PayrollResultDetailRepository;

class PayrollResultDetailServiceImplTest {

    @Mock
    private PayrollResultDetailRepository payrollResultDetailRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    private PayrollResultDetailServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        service = new PayrollResultDetailServiceImpl(payrollResultDetailRepository, transactionManager);
    }

    @Test
    void replacePayrollResultDetailsBestEffort_continuesWhenOneDetailFails() {
        PayrollResult payrollResult = com.dat.erp.testutils.EntityTestData.create(PayrollResult.class);
        com.dat.erp.testutils.EntityTestData.setCode(payrollResult, "PRR-1");
        PayrollResultDetail failedDetail = detail("PRD-FAIL", BigDecimal.TEN);
        PayrollResultDetail successfulDetail = detail("PRD-OK", BigDecimal.valueOf(20));

        when(payrollResultDetailRepository.findByPayrollResult_CodeAndIsDeletedFalse("PRR-1"))
                .thenReturn(List.of());
        doAnswer(invocation -> {
            PayrollResultDetail detail = invocation.getArgument(0);
            if (detail == failedDetail) {
                throw new RuntimeException("database constraint failed");
            }
            return detail;
        }).when(payrollResultDetailRepository).saveAndFlush(any(PayrollResultDetail.class));

        service.replacePayrollResultDetailsBestEffort(
                "TEST_OPERATION", payrollResult, List.of(failedDetail, successfulDetail));

        ArgumentCaptor<PayrollResultDetail> detailCaptor = ArgumentCaptor.forClass(PayrollResultDetail.class);
        verify(payrollResultDetailRepository, times(2)).saveAndFlush(detailCaptor.capture());
        assertSame(failedDetail, detailCaptor.getAllValues().get(0));
        assertSame(successfulDetail, detailCaptor.getAllValues().get(1));
        verify(transactionManager, times(1)).rollback(any());
        verify(transactionManager, times(1)).commit(any());
    }

    @Test
    void replacePayrollResultDetailsBestEffort_defersUntilCurrentTransactionCommits() {
        PayrollResult payrollResult = com.dat.erp.testutils.EntityTestData.create(PayrollResult.class);
        com.dat.erp.testutils.EntityTestData.setCode(payrollResult, "PRR-1");
        PayrollResultDetail detail = detail("PRD-OK", BigDecimal.valueOf(20));

        when(payrollResultDetailRepository.findByPayrollResult_CodeAndIsDeletedFalse("PRR-1"))
                .thenReturn(List.of());

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            service.replacePayrollResultDetailsBestEffort("TEST_OPERATION", payrollResult, List.of(detail));

            verify(payrollResultDetailRepository, never()).saveAndFlush(any(PayrollResultDetail.class));

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());
            synchronizations.get(0).afterCommit();

            verify(payrollResultDetailRepository).saveAndFlush(detail);
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private PayrollResultDetail detail(String code, BigDecimal amount) {
        PayrollResultDetail detail = new PayrollResultDetail();
        com.dat.erp.testutils.EntityTestData.setCode(detail, code);
        detail.setCalcBasis(PayrollResultCalcBasis.HOURS);
        detail.setAmount(amount);
        detail.setFormulaNote("test detail");
        return detail;
    }
}
