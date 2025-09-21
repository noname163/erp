package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.RoleHasApi;

@Repository
public interface RoleHasApiRepository extends JpaRepository<RoleHasApi, Long> {

}
