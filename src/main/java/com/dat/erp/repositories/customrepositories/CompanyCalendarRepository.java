package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.CompanyCalendar;

@Repository
public interface CompanyCalendarRepository extends JpaRepository<CompanyCalendar, Long> {
    Optional<CompanyCalendar> findByCodeAndIsDeletedFalse(String code);

    List<CompanyCalendar> findByCompanyCodeAndIsDeletedFalseOrderByEffectiveFromDesc(String companyCode);

    @Query("""
            select c
            from CompanyCalendar c
            where c.companyCode = :companyCode
              and c.isDeleted = false
              and c.effectiveFrom <= :periodEnd
              and c.effectiveTo >= :periodStart
            order by c.effectiveFrom asc
            """)
    List<CompanyCalendar> findOverlappingCalendars(
            @Param("companyCode") String companyCode,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd);
}
