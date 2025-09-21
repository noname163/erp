package com.dat.erp.customannotation.ownership;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.dat.erp.systemconfigs.CustomUserDetails;

@Component
public class OwnershipChecker {

    public boolean hasOwnership(Object entity, Authentication auth) {
        Class<?> clazz = entity.getClass();

        OwnableEntity annotation = clazz.getAnnotation(OwnableEntity.class);
        if (annotation == null) {
            throw new IllegalStateException("Entity " + clazz.getSimpleName() + " is not @OwnableEntity");
        }

        // Extract user details
        String username = auth.getName();
        Object principal = auth.getPrincipal();

        // Custom: maybe your UserDetails has tenantIds, groupIds, etc.
        Set<Object> allowedValues = new HashSet<>();
        allowedValues.add(username);

        if (principal instanceof CustomUserDetails userDetails) {
            // allowedValues.addAll(userDetails.getTenantIds());
            // allowedValues.addAll(userDetails.getCompanyIds());
        }

        // Check all declared ownership fields
        for (String fieldName : annotation.fields()) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(entity);

                if (value != null && allowedValues.contains(value)) {
                    return true; // ✅ at least one match
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to check ownership on " + clazz.getSimpleName(), e);
            }
        }

        return false; // ❌ no match found
    }
}
