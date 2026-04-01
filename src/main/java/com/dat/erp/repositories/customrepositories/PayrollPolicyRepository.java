package com.dat.erp.repositories.customrepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollPolicy;

@Repository
public interface PayrollPolicyRepository extends JpaRepository<PayrollPolicy, Long> {
    Optional<PayrollPolicy> findByCodeAndIsDeletedFalse(String code);

    @Query("""
            select pp
            from PayrollPolicy pp
            left join fetch pp.unit unit
            where pp.companyCode = :companyCode
              and pp.isDeleted = false
              and (coalesce(trim(:name), '') = ''
                   or lower(pp.name) like lower(concat('%', trim(coalesce(:name, '')), '%')))
              and (coalesce(trim(:unitCode), '') = '' or unit.code = trim(coalesce(:unitCode, '')))
              and pp.effectiveTo >= coalesce(:effectiveFrom, pp.effectiveTo)
              and pp.effectiveFrom <= coalesce(:effectiveTo, pp.effectiveFrom)
            order by pp.effectiveFrom desc, pp.updatedAt desc
            """)
    List<PayrollPolicy> findByFilters(
            @Param("companyCode") String companyCode,
            @Param("name") String name,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo,
            @Param("unitCode") String unitCode);

    @Query("""
            select count(pp) > 0
            from PayrollPolicy pp
            where pp.companyCode = :companyCode
              and pp.isDeleted = false
              and lower(pp.name) = lower(:name)
              and pp.effectiveFrom <= :effectiveTo
              and pp.effectiveTo >= :effectiveFrom
            """)
    boolean existsOverlappingByNameAndCompanyCode(
            @Param("name") String name,
            @Param("companyCode") String companyCode,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);
}
