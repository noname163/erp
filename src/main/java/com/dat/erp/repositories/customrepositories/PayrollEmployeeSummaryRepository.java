package com.dat.erp.repositories.customrepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollEmployeeSummary;

@Repository
public interface PayrollEmployeeSummaryRepository extends JpaRepository<PayrollEmployeeSummary, Long> {
    List<PayrollEmployeeSummary> findByPayrollRun_CodeAndIsDeletedFalseOrderByUserProfile_CodeAsc(String payrollRunCode);

    Optional<PayrollEmployeeSummary> findByPayrollRun_CodeAndUserProfile_CodeAndIsDeletedFalse(
            String payrollRunCode,
            String userProfileCode);

    boolean existsByPayrollRun_CodeAndHasBlockingIssueTrueAndIsDeletedFalse(String payrollRunCode);
}
