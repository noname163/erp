package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.entities.EmployeeProductionResult;

@Repository
public interface EmployeeProductionResultRepository extends JpaRepository<EmployeeProductionResult, Long> {

    @Query("""
            select epr
            from EmployeeProductionResult epr
            join fetch epr.userProfile up
            left join fetch epr.unit unit
            where epr.companyCode = :companyCode
              and epr.isDeleted = false
              and up.isDeleted = false
              and up.code = :employeeCode
              and epr.workDate >= :fromDate
              and epr.workDate <= :toDate
              and epr.approvalStatus = :approvalStatus
            order by epr.workDate asc, epr.productCode asc
            """)
    List<EmployeeProductionResult> findApprovedByEmployeeAndDateRange(
            @Param("employeeCode") String employeeCode,
            @Param("companyCode") String companyCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("approvalStatus") ApprovalStatus approvalStatus);
}
