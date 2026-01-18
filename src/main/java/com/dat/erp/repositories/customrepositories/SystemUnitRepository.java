package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.SystemUnit;

@Repository
public interface SystemUnitRepository extends JpaRepository<SystemUnit, Long> {
    Optional<SystemUnit> findByCode(String code);
}

