package com.dat.erp.systemconfigs;

import java.util.List;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
public class AuditableFilterManager {

    private final EntityManager entityManager;

    public AuditableFilterManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void enable() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal();

            if (customUserDetails.getViewOwnedOnly() && !customUserDetails.getEmployeeCodes().isEmpty()) {
                List<String> createdByCodes = customUserDetails.getEmployeeCodes();
                List<String> updatedByCodes = customUserDetails.getEmployeeCodes();
                Session session = entityManager.unwrap(Session.class);
                Filter filter = session.enableFilter("auditableFilter");
                filter.setParameterList("createdByList", createdByCodes);
                filter.setParameterList("updatedByList", updatedByCodes);
            }
        }
    }

    public void disable() {
        entityManager.unwrap(Session.class).disableFilter("auditableFilter");
    }
}
