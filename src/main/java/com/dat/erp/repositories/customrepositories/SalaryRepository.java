package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.Salary;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {
    Optional<Salary> findByCode(String code);

    List<Salary> findAllByCodeIn(Collection<String> codes);

    @Query("""
            select upper(s.name)
            from Salary s
            where s.companyCode = :companyCode
              and s.isDeleted = false
              and upper(s.name) in :names
            """)
    List<String> findExistingUpperCaseNamesByCompanyCodeAndIsDeletedFalse(
            @Param("companyCode") String companyCode,
            @Param("names") Collection<String> names);

    boolean existsByNameIgnoreCaseAndCompanyCodeAndIsDeletedFalse(String name, String companyCode);

    @Query("""
            select s
            from Salary s
            where s.companyCode = :companyCode
              and s.isDeleted = false
              and (coalesce(:name, '') = '' or lower(s.name) like lower(concat('%', coalesce(:name, ''), '%')))
            """)
    Page<Salary> findOptionsByFilters(@Param("companyCode") String companyCode, @Param("name") String name,
            Pageable pageable);
}
