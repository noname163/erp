package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long>, JpaSpecificationExecutor<UserProfile> {
    Optional<UserProfile> findByAccount_Code(String accountCode);

    Optional<UserProfile> findByCode(String code);

    @Override
    @EntityGraph(attributePaths = { "account", "department" })
    org.springframework.data.domain.Page<UserProfile> findAll(
            org.springframework.data.jpa.domain.Specification<UserProfile> spec,
            org.springframework.data.domain.Pageable pageable);
}
