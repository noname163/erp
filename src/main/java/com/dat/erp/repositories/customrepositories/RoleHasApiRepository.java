package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.RoleHasApi;

@Repository
public interface RoleHasApiRepository extends JpaRepository<RoleHasApi, Long> {
    @Query("SELECT r FROM RoleHasApi r JOIN SystemApi sa ON sa.code = r.api.code WHERE r.role.code = :roleCode")
    Page<RoleHasApi> findByRoleCode(@Param("roleCode") String roleCode, Pageable pageable);

    @Query("""
                SELECT sa.endpoint, rha.permission
                FROM RoleHasApi rha
                JOIN rha.role r
                JOIN rha.api sa
                JOIN EmployeeInformation ei ON ei.role = r
                WHERE ei.code = :employeeCode
                  AND rha.isDeleted = false
            """)
    List<Object[]> getCurrentUserPermission(@Param("employeeCode") String employeeCode);

    boolean existsByRole_CodeAndApi_Code(String roleCode, String apiCode);

}
