package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.constants.ApprovalStatus;
import com.dat.erp.entities.EmployeeKpiResult;

@Repository
public interface EmployeeKpiResultRepository extends JpaRepository<EmployeeKpiResult, Long> {

    @Query("""
            select ekr
            from EmployeeKpiResult ekr
            join fetch ekr.userProfile up
            where ekr.companyCode = :companyCode
              and ekr.isDeleted = false
              and up.isDeleted = false
              and up.code = :employeeCode
              and ekr.payrollPeriod = :payrollPeriod
              and ekr.approvalStatus = :approvalStatus
            order by ekr.kpiCode asc
            """)
    List<EmployeeKpiResult> findApprovedByEmployeeAndPeriod(
            @Param("employeeCode") String employeeCode,
            @Param("companyCode") String companyCode,
            @Param("payrollPeriod") String payrollPeriod,
            @Param("approvalStatus") ApprovalStatus approvalStatus);
}
