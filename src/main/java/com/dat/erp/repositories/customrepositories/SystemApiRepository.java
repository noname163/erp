package com.dat.erp.repositories.customrepositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.SystemApi;

@Repository
public interface SystemApiRepository extends JpaRepository<SystemApi, Long> {
    Optional<SystemApi> findByCode(String code);

    List<SystemApi> findByEndpointIn(Set<String> endpoint);
}
