package com.dat.erp.repositories.autorepositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DynamicJpaRepository<T, ID> extends JpaRepository<T, ID> {

    // Multiple results
    List<T> findAllByAuto(Object searchDto);

    // Single result (throws if more than one)
    T findByAuto(Object searchDto);
}
