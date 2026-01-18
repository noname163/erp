package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.UserIdentity;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
}

