package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollResult;

@Repository
public interface PayrollResultRepository extends JpaRepository<PayrollResult, Long> {
    List<PayrollResult> findByPayrollRun_CodeAndIsDeletedFalse(String payrollRunCode);
}
