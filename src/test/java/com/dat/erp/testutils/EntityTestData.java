package com.dat.erp.testutils;

import org.springframework.test.util.ReflectionTestUtils;

import com.dat.erp.entities.BaseAuditableEntity;

/**
 * Sets persistence-managed identity fields when arranging unit-test fixtures.
 * Production code intentionally exposes these fields as read-only.
 */
public final class EntityTestData {

    private EntityTestData() {
    }

    public static <T> T create(Class<T> entityType) {
        try {
            var constructor = entityType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Cannot create test entity " + entityType.getName(), ex);
        }
    }

    public static void setField(Object entity, String fieldName, Object value) {
        ReflectionTestUtils.setField(entity, fieldName, value);
    }

    public static void setId(BaseAuditableEntity entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }

    public static void setCode(BaseAuditableEntity entity, String code) {
        ReflectionTestUtils.setField(entity, "code", code);
    }

    public static void setCompanyCode(BaseAuditableEntity entity, String companyCode) {
        ReflectionTestUtils.setField(entity, "companyCode", companyCode);
    }
}
