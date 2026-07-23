package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollResultDetail;

@Repository
public interface PayrollResultDetailRepository extends JpaRepository<PayrollResultDetail, Long> {
    List<PayrollResultDetail> findByPayrollResult_CodeAndIsDeletedFalse(String payrollResultCode);
}
