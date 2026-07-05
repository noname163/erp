package com.dat.erp.repositories.customrepositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.constants.PayrollStatus;
import com.dat.erp.entities.PayrollResult;
import com.dat.erp.repositories.projections.PayrollResultListProjection;

@Repository
public interface PayrollResultRepository extends JpaRepository<PayrollResult, Long> {
    @Query("""
            select
                payrollRun.code as payrollRunCode,
                st.name as salaryName,
                pr.expectedAmount as expectedAmount,
                trim(concat(concat(coalesce(up.firstName, ''), ' '), coalesce(up.lastName, ''))) as employeeName,
                pr.actualAmount as actualAmount,
                pr.currency as currency,
                pr.expectedQuantity as expectedQuantity,
                pr.actualQuantity as actualQuantity,
                unit.name as unitName,
                pr.sourceType as sourceType,
                pr.isRetro as isRetro,
                pr.retroReason as retroReason,
                payrollRun.period as period,
                pr.createdAt as createdAt,
                up.code as employeeCode
            from PayrollResult pr
            join pr.payrollRun payrollRun 
            join pr.employeeSalary es
            join es.userProfile up
            left join es.salaryTemplate st
            left join pr.unit unit
            where pr.companyCode = :companyCode
              and pr.isDeleted = false
              and up.isDeleted = false
              and payrollRun.code = :payrollRunCode
              and pr.createdAt >= :createdAtFrom
              and pr.createdAt < :createdAtTo
              and pr.sourceType = coalesce(:sourceType, pr.sourceType)
              and up.code = coalesce(:employeeCode, up.code)
            """)
    Page<PayrollResultListProjection> searchByConditions(
            @Param("companyCode") String companyCode,
            @Param("payrollRunCode") String payrollRunCode,
            @Param("createdAtFrom") LocalDateTime createdAtFrom,
            @Param("createdAtTo") LocalDateTime createdAtTo,
            @Param("sourceType") PayrollStatus sourceType,
            @Param("employeeCode") String employeeCode,
            Pageable pageable);

    List<PayrollResult> findByPayrollRun_CodeAndIsDeletedFalse(String payrollRunCode);

    Optional<PayrollResult> findByCodeAndCompanyCodeAndIsDeletedFalse(String code, String companyCode);
}
