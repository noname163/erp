package com.dat.erp.repositories.customrepositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.dat.erp.constants.SystemUnitType;
import com.dat.erp.entities.SystemUnit;


public interface SystemUnitRepository extends JpaRepository<SystemUnit, Long> {
    Optional<SystemUnit> findByCode(String code);

    Optional<SystemUnit> findByCodeAndIsDeletedFalse(String code);

    List<SystemUnit> findAllByCodeIn(Collection<String> codes);

    @Query("""
            select su
            from SystemUnit su
            where su.isDeleted = false
              and (coalesce(:name, '') = '' or lower(su.name) like lower(concat('%', coalesce(:name, ''), '%')))
              and su.type = coalesce(:type, su.type)
            """)
    Page<SystemUnit> findOptionsByFilters(@Param("name") String name,
            @Param("type") SystemUnitType type, Pageable pageable);
}
