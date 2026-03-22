package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollPolicy;

@Repository
public interface PayrollPolicyRepository extends JpaRepository<PayrollPolicy, Long> {
    Optional<PayrollPolicy> findByCodeAndIsDeletedFalse(String code);

    List<PayrollPolicy> findByCompanyCodeAndIsDeletedFalseOrderByEffectiveFromDesc(String companyCode);

    @Query("""
            select p
            from PayrollPolicy p
            where p.companyCode = :companyCode
              and p.isDeleted = false
              and p.effectiveFrom <= :periodEnd
              and p.effectiveTo >= :periodStart
            order by p.effectiveFrom asc
            """)
    List<PayrollPolicy> findOverlappingPolicies(
            @Param("companyCode") String companyCode,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
