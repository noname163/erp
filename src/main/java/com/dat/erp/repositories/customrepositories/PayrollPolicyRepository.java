package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollPolicy;

@Repository
public interface PayrollPolicyRepository extends JpaRepository<PayrollPolicy, Long> {
    Optional<PayrollPolicy> findByCodeAndIsDeletedFalse(String code);

    @Query("""
            select count(pp) > 0
            from PayrollPolicy pp
            where pp.companyCode = :companyCode
              and pp.isDeleted = false
              and lower(pp.name) = lower(:name)
              and pp.effectiveFrom <= :effectiveTo
              and pp.effectiveTo >= :effectiveFrom
            """)
    boolean existsOverlappingByNameAndCompanyCode(
            @Param("name") String name,
            @Param("companyCode") String companyCode,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);
}
