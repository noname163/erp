package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmployeePayrollPolicy;

@Repository
public interface EmployeePayrollPolicyRepository extends JpaRepository<EmployeePayrollPolicy, Long> {
    Optional<EmployeePayrollPolicy> findByCodeAndIsDeletedFalse(String code);

    List<EmployeePayrollPolicy> findByUserProfile_CodeAndIsDeletedFalseOrderByEffectiveFromDesc(String userProfileCode);

    @Query("""
            select case when count(epp) > 0 then true else false end
            from EmployeePayrollPolicy epp
            where epp.userProfile.code = :userProfileCode
              and epp.isDeleted = false
              and epp.isActive = true
              and (:ignoreCode is null or epp.code <> :ignoreCode)
              and epp.effectiveFrom <= :effectiveTo
              and epp.effectiveTo >= :effectiveFrom
            """)
    boolean existsActiveOverlap(
            @Param("userProfileCode") String userProfileCode,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("ignoreCode") String ignoreCode);
}
