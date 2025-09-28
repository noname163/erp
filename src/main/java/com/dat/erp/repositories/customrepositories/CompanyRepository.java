package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dat.erp.entities.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCode(String code);
}
