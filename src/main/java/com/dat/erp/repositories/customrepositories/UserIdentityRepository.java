package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;


import com.dat.erp.entities.UserIdentity;


public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
}

