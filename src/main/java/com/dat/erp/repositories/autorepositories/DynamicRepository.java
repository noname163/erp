package com.dat.erp.repositories.autorepositories;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.dat.erp.customannotation.searchable.interfaces.Searchable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class DynamicRepository<T, ID> {
    private final Class<T> entityClass;
    private final EntityManager em;
    private final List<Field> searchableFields;

    public DynamicRepository(Class<T> entityClass, EntityManager em, List<Field> searchableFields) {
        this.entityClass = entityClass;
        this.em = em;
        this.searchableFields = searchableFields;
    }

    public List<T> findAllByCriteria(Object searchDto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);
        Root<T> root = query.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();

        for (Field field : searchableFields) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(searchDto);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (value == null)
                continue;

            Searchable searchable = field.getAnnotation(Searchable.class);
            String path = searchable.path().isEmpty() ? field.getName() : searchable.path();
            Path<?> expression = getPath(root, path);

            Predicate predicate = buildPredicate(cb, expression, searchable, value);
            predicates.add(predicate);
        }

        query.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
        return em.createQuery(query).getResultList();
    }

    public T findOneByCriteria(Object searchDto) {
        List<T> results = findAllByCriteria(searchDto);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new NonUniqueResultException("More than one result found for findByAuto");
        }
        return results.get(0);
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Path<?> expression,
            Searchable searchable, Object value) {
        return switch (searchable.condition()) {
            case LIKE -> cb.like(cb.lower(expression.as(String.class)),
                    "%" + value.toString().toLowerCase() + "%");
            case GREATER_THAN -> cb.greaterThan(expression.as(Comparable.class), (Comparable) value);
            case LESS_THAN -> cb.lessThan(expression.as(Comparable.class), (Comparable) value);
            default -> cb.equal(expression, value);
        };
    }

    private Path<?> getPath(Root<T> root, String path) {
        if (path.contains(".")) {
            String[] parts = path.split("\\.");
            Path<?> p = root.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                p = p.get(parts[i]);
            }
            return p;
        }
        return root.get(path);
    }
}
