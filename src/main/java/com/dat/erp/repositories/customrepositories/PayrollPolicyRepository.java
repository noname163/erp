package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.PayrollPolicy;

@Repository
public interface PayrollPolicyRepository extends JpaRepository<PayrollPolicy, Long> {
    Optional<PayrollPolicy> findByCodeAndIsDeletedFalse(String code);
}
