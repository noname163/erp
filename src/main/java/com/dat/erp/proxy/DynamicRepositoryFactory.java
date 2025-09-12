package com.dat.erp.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dat.erp.customannotation.searchable.interfaces.Searchable;
import com.dat.erp.repositories.autorepositories.DynamicJpaRepository;
import com.dat.erp.repositories.autorepositories.DynamicRepository;

import jakarta.persistence.EntityManager;

public class DynamicRepositoryFactory {

    public static <T, ID> DynamicJpaRepository<T, ID> create(
            Class<T> entityClass,
            EntityManager em,
            JpaRepository<T, ID> baseRepo) {

        return (DynamicJpaRepository<T, ID>) Proxy.newProxyInstance(
                DynamicJpaRepository.class.getClassLoader(),
                new Class[] { DynamicJpaRepository.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("findAllByAuto")) {
                        Object searchDto = args[0];
                        return new DynamicRepository<>(entityClass, em,
                                getSearchableFields(searchDto)).findAllByCriteria(searchDto);
                    }
                    if (method.getName().equals("findByAuto")) {
                        Object searchDto = args[0];
                        return new DynamicRepository<>(entityClass, em,
                                getSearchableFields(searchDto)).findOneByCriteria(searchDto);
                    }
                    return method.invoke(baseRepo, args);
                });
    }

    private static List<Field> getSearchableFields(Object dto) {
        return Arrays.stream(dto.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Searchable.class))
                .toList();
    }
}
