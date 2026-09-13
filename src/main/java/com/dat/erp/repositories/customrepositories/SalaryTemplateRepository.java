package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.SalaryTemplate;


public interface SalaryTemplateRepository extends JpaRepository<SalaryTemplate, Long> {
    @Query("""
            select st
            from SalaryTemplate st
            where st.companyCode = :companyCode
              and st.isDeleted = false
              and (coalesce(trim(:name), '') = '' or lower(st.name) like lower(concat('%', coalesce(:name, ''), '%')))
              and (coalesce(trim(:currency), '') = '' or upper(st.currency) = upper(coalesce(:currency, '')))
              and st.effectiveTo >= coalesce(:effectiveFrom, st.effectiveTo)
              and st.effectiveFrom <= coalesce(:effectiveTo, st.effectiveFrom)
            """)
    Page<SalaryTemplate> searchByConditions(
            @Param("companyCode") String companyCode,
            @Param("name") String name,
            @Param("currency") String currency,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            Pageable pageable);

    @Query("""
            select st
            from SalaryTemplate st
            where st.companyCode = :companyCode
              and st.isDeleted = false
              and (coalesce(trim(:name), '') = '' or lower(st.name) like lower(concat('%', coalesce(:name, ''), '%')))
            """)
    Page<SalaryTemplate> findOptionsByFilters(@Param("companyCode") String companyCode, @Param("name") String name,
            Pageable pageable);

    Optional<SalaryTemplate> findByCodeAndCompanyCodeAndIsDeletedFalse(String code, String companyCode);

    @Query("""
            select count(st) > 0
            from SalaryTemplate st
            where st.companyCode = :companyCode
              and lower(st.name) = lower(:name)
              and st.effectiveFrom <= :effectiveTo
              and st.effectiveTo >= :effectiveFrom
            """)
    boolean existsOverlappingByNameAndCompanyCode(
            @Param("name") String name,
            @Param("companyCode") String companyCode,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);
}
