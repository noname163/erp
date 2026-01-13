package com.dat.erp.repositories.customrepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dat.erp.entities.Email;

public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findTop50ByNeedRetryAndIsSentFalseOrderByIdAsc(boolean needRetry);
}
