package com.dat.erp.repositories.customrepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.dat.erp.constants.CommonStatus;
import com.dat.erp.entities.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);

    Optional<Department> findByCodeAndCompanyCode(String code, String companyCode);

    Optional<Department> findByNameAndCompanyCode(String name, String companyCode);

    boolean existsByNameAndCompanyCode(String name, String companyCode);

    @Query("""
            SELECT d
            FROM Department d
            WHERE d.companyCode = :companyCode
              AND d.status = :status
              AND d.isDeleted = false
              AND (:name IS NULL OR TRIM(:name) = '' OR d.name = :name)
            ORDER BY d.name ASC
            """)
    List<Department> findByNameAndCompanyCodeAndStatusAndIsDeletedFalseOrderByNameAsc(@Param("name") String name,
            @Param("companyCode") String companyCode, @Param("status") CommonStatus status);
}
