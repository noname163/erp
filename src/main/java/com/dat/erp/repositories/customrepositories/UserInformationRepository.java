package com.dat.erp.repositories.customrepositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dat.erp.entities.UserInformation;

@Repository
public interface UserInformationRepository extends JpaRepository<UserInformation, Long> {

}
