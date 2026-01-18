package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.SalaryTemplate;

@Repository
public interface SalaryTemplateRepository extends JpaRepository<SalaryTemplate, Long> {
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
            @Param("effectiveFrom") java.time.LocalDate effectiveFrom,
            @Param("effectiveTo") java.time.LocalDate effectiveTo);
}

