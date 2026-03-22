package com.dat.erp.repositories.customrepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollAdjustment;

@Repository
public interface PayrollAdjustmentRepository extends JpaRepository<PayrollAdjustment, Long> {
    Optional<PayrollAdjustment> findByCodeAndIsDeletedFalse(String code);

    List<PayrollAdjustment> findByCompanyCodeAndIsDeletedFalseOrderByEffectivePayrollMonthDescCreatedAtDesc(String companyCode);

    List<PayrollAdjustment> findByCompanyCodeAndUserProfile_CodeAndEffectivePayrollMonthAndIsDeletedFalse(
            String companyCode,
            String userProfileCode,
            String effectivePayrollMonth);
}
