package com.dat.erp.repositories.customrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;


import com.dat.erp.entities.CodeSequence;

import jakarta.persistence.LockModeType;


public interface CodeSequenceRepository extends JpaRepository<CodeSequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CodeSequence> findByPrefix(String prefix);
}

