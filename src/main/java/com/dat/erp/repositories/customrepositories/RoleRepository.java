package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

}
