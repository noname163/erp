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

    @Query("""
            select epp
            from EmployeePayrollPolicy epp
            join fetch epp.userProfile up
            join fetch epp.payrollPolicy pp
            where up.code = :userProfileCode
              and epp.isDeleted = false
            order by epp.effectiveFrom desc
            """)
    List<EmployeePayrollPolicy> findByUserProfileCodeAndIsDeletedFalseOrderByEffectiveFromDesc(
            @Param("userProfileCode") String userProfileCode);

    @Query("""
            select case when count(epp) > 0 then true else false end
            from EmployeePayrollPolicy epp
            where epp.userProfile.code = :userProfileCode
              and epp.isDeleted = false
              and epp.isActive = true
              and (coalesce(trim(:ignoreCode), '') = '' or epp.code <> trim(coalesce(:ignoreCode, '')))
              and epp.effectiveFrom <= :effectiveTo
              and epp.effectiveTo >= :effectiveFrom
            """)
    boolean existsActiveOverlap(
            @Param("userProfileCode") String userProfileCode,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("ignoreCode") String ignoreCode);

    @Query("""
            select distinct up.code
            from EmployeePayrollPolicy epp
            join epp.userProfile up
            where epp.companyCode = :companyCode
              and epp.isDeleted = false
              and epp.isActive = true
              and up.code in :userProfileCodes
              and epp.effectiveFrom <= :effectiveTo
              and epp.effectiveTo >= :effectiveFrom
            order by up.code asc
            """)
    List<String> findActiveOverlapUserProfileCodes(
            @Param("companyCode") String companyCode,
            @Param("userProfileCodes") List<String> userProfileCodes,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);

    @Query("""
            select epp
            from EmployeePayrollPolicy epp
            join fetch epp.userProfile up
            join fetch epp.payrollPolicy pp
            where epp.companyCode = :companyCode
              and epp.isDeleted = false
              and epp.isActive = true
              and up.isDeleted = false
              and pp.isDeleted = false
              and up.code in :employeeCodes
              and epp.effectiveFrom <= :date
              and epp.effectiveTo >= :date
              and pp.effectiveFrom <= :date
              and pp.effectiveTo >= :date
            order by up.code asc, epp.effectiveFrom desc, epp.updatedAt desc
            """)
    List<EmployeePayrollPolicy> findActivePoliciesByEmployeeCodesAndDate(
            @Param("companyCode") String companyCode,
            @Param("employeeCodes") List<String> employeeCodes,
            @Param("date") LocalDate date);
}
