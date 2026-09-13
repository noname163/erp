package com.dat.erp.repositories.customrepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.entities.Role;


public interface RoleRepository extends JpaRepository<Role, Long> {
    public Optional<Role> findByCode(String code);

    public Optional<Role> findByName(String name);

    public Optional<Role> findByType(String type);

    @Query("""
            SELECT r
            FROM Role r
            WHERE r.isDeleted = false
              AND (
                    (:isPublic = true AND r.isPublic = true)
                    OR
                    ((:isPublic IS NULL OR :isPublic = false)
                     AND (:isPublic IS NULL OR r.isPublic = false)
                     AND (:companyCode IS NULL OR TRIM(:companyCode) = '' OR r.companyCode = :companyCode))
                  )
              AND (:name IS NULL OR TRIM(:name) = '' OR r.name = :name)
            ORDER BY r.name ASC
            """)
    List<Role> findOptionsByFilters(@Param("name") String name, @Param("isPublic") Boolean isPublic,
            @Param("companyCode") String companyCode);
}
