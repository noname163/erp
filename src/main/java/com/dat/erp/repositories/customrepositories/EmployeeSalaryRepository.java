package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.EmployeeSalary;

@Repository
public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, Long> {
    java.util.Optional<EmployeeSalary> findByCodeAndIsDeletedFalse(String code);

    @Query("""
            select count(es) > 0
            from EmployeeSalary es
            where es.companyCode = :companyCode
              and es.isDeleted = false
              and es.userProfile.code = :userProfileCode
              and es.effectiveFrom <= :effectiveTo
              and es.effectiveTo >= :effectiveFrom
            """)
    boolean existsOverlappingByUserProfileCodeAndCompanyCode(
            @Param("userProfileCode") String userProfileCode,
            @Param("companyCode") String companyCode,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);
}
