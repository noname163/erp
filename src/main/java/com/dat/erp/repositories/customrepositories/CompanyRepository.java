package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dat.erp.entities.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {

}
