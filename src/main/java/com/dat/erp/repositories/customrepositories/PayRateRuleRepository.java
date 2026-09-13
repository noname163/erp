package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.PayRateRule;


public interface PayRateRuleRepository extends JpaRepository<PayRateRule, Long> {

    @Query("""
            select prr
            from PayRateRule prr
            join prr.policy policy
            where policy.code = :policyCode
              and prr.companyCode = :companyCode
              and prr.isDeleted = false
              and policy.isDeleted = false
              and prr.effectiveFrom <= :periodEnd
              and prr.effectiveTo >= :periodStart
            order by prr.effectiveFrom desc, prr.updatedAt desc
            """)
    List<PayRateRule> findActiveByPolicyCodeAndCompanyCodeAndPeriod(
            @Param("policyCode") String policyCode,
            @Param("companyCode") String companyCode,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
