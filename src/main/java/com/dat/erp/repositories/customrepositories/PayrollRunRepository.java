package com.dat.erp.repositories.customrepositories;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

import com.dat.erp.constants.PayrollRunStatus;
import com.dat.erp.entities.PayrollRun;

import jakarta.persistence.LockModeType;


public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {
    @Query("""
            select pr
            from PayrollRun pr
            where pr.companyCode = :companyCode
              and pr.isDeleted = false
              and pr.status = coalesce(:status, pr.status)
              and coalesce(pr.runAt, :runAtNullValue) >= :runAtFrom
              and coalesce(pr.runAt, :runAtNullValue) <= :runAtTo
              and coalesce(pr.closedAt, :closeAtNullValue) >= :closeAtFrom
              and coalesce(pr.closedAt, :closeAtNullValue) <= :closeAtTo
            """)
    Page<PayrollRun> searchByConditions(
            @Param("companyCode") String companyCode,
            @Param("status") PayrollRunStatus status,
            @Param("runAtFrom") LocalDateTime runAtFrom,
            @Param("runAtTo") LocalDateTime runAtTo,
            @Param("runAtNullValue") LocalDateTime runAtNullValue,
            @Param("closeAtFrom") LocalDateTime closeAtFrom,
            @Param("closeAtTo") LocalDateTime closeAtTo,
            @Param("closeAtNullValue") LocalDateTime closeAtNullValue,
            Pageable pageable);

    Optional<PayrollRun> findByCompanyCodeAndPeriodAndIsDeletedFalse(String companyCode, YearMonth period);

    Optional<PayrollRun> findByCodeAndCompanyCodeAndIsDeletedFalse(String code, String companyCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select pr
            from PayrollRun pr
            where pr.code = :code
              and pr.companyCode = :companyCode
              and pr.isDeleted = false
            """)
    Optional<PayrollRun> findLockedByCodeAndCompanyCode(
            @Param("code") String code,
            @Param("companyCode") String companyCode);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update PayrollRun pr
            set pr.status = case
                when :success = true then com.dat.erp.constants.PayrollRunStatus.CALCULATED
                else com.dat.erp.constants.PayrollRunStatus.FAILED
            end
            where pr.code = :code
              and pr.companyCode = :companyCode
              and pr.isDeleted = false
            """)
    int updateStatusBySuccessFlag(
            @Param("code") String code,
            @Param("companyCode") String companyCode,
            @Param("success") boolean success);

    
}
