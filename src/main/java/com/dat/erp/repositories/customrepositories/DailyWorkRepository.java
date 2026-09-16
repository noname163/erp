package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.DailyWork;
import com.dat.erp.repositories.projections.EmployeeDailyWorkListProjection;


public interface DailyWorkRepository extends JpaRepository<DailyWork, Long> {
    boolean existsByUserProfile_CodeAndWorkingDateAndIsDeletedFalse(String userProfileCode,
            java.time.LocalDate workingDate);

    @Query("""
            select dw
            from DailyWork dw
            join fetch dw.userProfile up
            where dw.isDeleted = false
              and up.isDeleted = false
              and up.code in :userProfileCodes
              and dw.workingDate in :workingDates
            """)
    List<DailyWork> findExistingByUserProfileCodesAndWorkingDates(
            @Param("userProfileCodes") Collection<String> userProfileCodes,
            @Param("workingDates") Collection<LocalDate> workingDates);

    @Query("""
            select
                up.code as employeeCode,
                trim(concat(concat(coalesce(up.firstName, ''), ' '), coalesce(up.lastName, ''))) as employeeName,
                dw.workType as workType,
                dw.workingDate as logDay,
                dw.startTime as startTime,
                dw.endTime as endTime,
                trim(concat(concat(coalesce(createdProfile.firstName, ''), ' '), coalesce(createdProfile.lastName, ''))) as createdByName,
                trim(concat(concat(coalesce(updatedProfile.firstName, ''), ' '), coalesce(updatedProfile.lastName, ''))) as editedByName,
                dw.otTime as otTime,
                dw.usedPto as usedPto
            from DailyWork dw
            join dw.userProfile up
            left join Account createdAccount on createdAccount.code = dw.createdBy
            left join createdAccount.userProfile createdProfile
            left join Account updatedAccount on updatedAccount.code = dw.updatedBy
            left join updatedAccount.userProfile updatedProfile
            where dw.companyCode = :companyCode
              and dw.isDeleted = false
              and up.isDeleted = false
              and up.code = coalesce(:employeeCode, up.code)
              and dw.workingDate >= coalesce(:startDate, dw.workingDate)
              and dw.workingDate <= coalesce(:endDate, dw.workingDate)
              and dw.usedPto = coalesce(:isPto, dw.usedPto)
            """)
    Page<EmployeeDailyWorkListProjection> findEmployeeDailyWorksByFilters(
            @Param("companyCode") String companyCode,
            @Param("employeeCode") String employeeCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isPto") Boolean isPto,
            Pageable pageable);

    @Query("""
            select dw
            from DailyWork dw
            join fetch dw.userProfile up
            where dw.companyCode = :companyCode
              and dw.isDeleted = false
              and up.isDeleted = false
              and up.code in :employeeCodes
            order by up.code asc, dw.workingDate asc, dw.startTime asc
            """)
    List<DailyWork> findAllForSalaryByCompanyCodeAndEmployeeCodes(
            @Param("companyCode") String companyCode,
            @Param("employeeCodes") List<String> employeeCodes);

    @Query("""
            select dw
            from DailyWork dw
            join fetch dw.userProfile up
            where dw.companyCode = :companyCode
              and dw.isDeleted = false
              and up.isDeleted = false
              and up.code in :employeeCodes
              and dw.workingDate >= :startDate
              and dw.workingDate <= :endDate
            order by up.code asc, dw.workingDate asc, dw.startTime asc
            """)
    List<DailyWork> findAllForSalaryByCompanyCodeAndEmployeeCodesAndWorkingDateBetween(
            @Param("companyCode") String companyCode,
            @Param("employeeCodes") List<String> employeeCodes,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
            select dw
            from DailyWork dw
            join fetch dw.userProfile up
            where dw.companyCode = :companyCode
              and dw.isDeleted = false
              and up.isDeleted = false
              and up.code = :employeeCode
              and dw.workingDate >= :fromDate
              and dw.workingDate <= :toDate
            order by dw.workingDate asc, dw.startTime asc
            """)
    List<DailyWork> findAllByEmployeeCodeAndCompanyCodeAndDateRange(
            @Param("employeeCode") String employeeCode,
            @Param("companyCode") String companyCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
