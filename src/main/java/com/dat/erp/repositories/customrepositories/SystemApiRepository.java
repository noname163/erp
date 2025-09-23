package com.dat.erp.repositories.customrepositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.SystemApi;

@Repository
public interface SystemApiRepository extends JpaRepository<SystemApi, Long> {
    Optional<SystemApi> findByEndpointAndMethodAndType(String endpoint, String method, String type);

    @Query("""
                SELECT s FROM SystemApi s
                WHERE (s.endpoint, s.method, s.type) IN :triplets
            """)
    List<SystemApi> findExistingByEndpointMethodAndType(@Param("triplets") List<Object[]> triplets);
}
