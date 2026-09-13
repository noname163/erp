package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.EmployeeSalary;


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

    @Query("""
            select es
            from EmployeeSalary es
            join fetch es.userProfile up
            where es.companyCode = :companyCode
              and es.isDeleted = false
              and (coalesce(trim(:employeeName), '') = ''
                   or lower(concat(coalesce(up.firstName, ''), ' ', coalesce(up.lastName, '')))
                      like lower(concat('%', trim(coalesce(:employeeName, '')), '%')))
              and es.effectiveTo >= coalesce(:effectiveFrom, es.effectiveTo)
              and es.effectiveFrom <= coalesce(:effectiveTo, es.effectiveFrom)
            """)
    List<EmployeeSalary> searchByConditions(
            @Param("companyCode") String companyCode,
            @Param("employeeName") String employeeName,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);

    @Query("""
            select es
            from EmployeeSalary es
            join fetch es.userProfile up
            where es.companyCode = :companyCode
              and es.isDeleted = false
              and up.code = :employeeCode
              and es.effectiveFrom <= :asOfDate
              and es.effectiveTo >= :asOfDate
            order by es.effectiveFrom desc
            """)
    List<EmployeeSalary> findActiveByEmployeeCodeAndCompanyCodeAndDate(
            @Param("employeeCode") String employeeCode,
            @Param("companyCode") String companyCode,
            @Param("asOfDate") LocalDate asOfDate);

    default Optional<EmployeeSalary> findFirstActiveByEmployeeCodeAndCompanyCodeAndDate(
            String employeeCode, String companyCode, LocalDate asOfDate) {
        return findActiveByEmployeeCodeAndCompanyCodeAndDate(employeeCode, companyCode, asOfDate).stream().findFirst();
    }

    @Query("""
            select es
            from EmployeeSalary es
            join fetch es.userProfile up
            where es.companyCode = :companyCode
              and es.isDeleted = false
              and up.isDeleted = false
              and up.code in :employeeCodes
              and es.effectiveFrom <= :date
              and es.effectiveTo >= :date
            order by up.code asc, es.effectiveFrom desc, es.updatedAt desc
            """)
    List<EmployeeSalary> findActiveByCompanyCodeAndUserProfileCodesAndDate(
            @Param("companyCode") String companyCode,
            @Param("employeeCodes") List<String> employeeCodes,
            @Param("date") LocalDate date);
}
