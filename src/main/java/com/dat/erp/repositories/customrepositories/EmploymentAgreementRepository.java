package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmploymentAgreement;

@Repository
public interface EmploymentAgreementRepository extends JpaRepository<EmploymentAgreement, Long> {
    @Query("""
            select e
            from EmploymentAgreement e
            where e.companyCode = :companyCode
              and e.userProfile.code = :userProfileCode
              and e.isDeleted = false
              and e.effectiveFrom <= :periodEnd
              and e.effectiveTo >= :periodStart
            order by e.effectiveFrom asc
            """)
    List<EmploymentAgreement> findOverlappingAgreements(
            @Param("companyCode") String companyCode,
            @Param("userProfileCode") String userProfileCode,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
