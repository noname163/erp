package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

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
