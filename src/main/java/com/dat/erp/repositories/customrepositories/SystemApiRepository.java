package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.SystemApi;

@Repository
public interface SystemApiRepository extends JpaRepository<SystemApi, Long> {
    Optional<SystemApi> findByCode(String code);
}
