package com.dat.erp.repositories.customrepositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.RoleHasApi;

@Repository
public interface RoleHasApiRepository extends JpaRepository<RoleHasApi, Long> {
    @Query("SELECT r FROM RoleHasApi r WHERE r.role.code = :roleCode")
    Page<RoleHasApi> findByRoleCode(@Param("roleCode") String roleCode, Pageable pageable);

}
