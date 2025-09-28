package com.dat.erp.systemconfigs;

import java.util.List;
import java.util.Optional;

import org.hibernate.Filter;
import org.hibernate.Session;
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
            if (SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal();

                if (Optional.ofNullable(
                        customUserDetails.getViewOwnedOnly()).orElse(false)
                        && !customUserDetails.getEmployeeCodes().isEmpty()) {
                    List<String> createdByCodes = customUserDetails.getEmployeeCodes();
                    List<String> updatedByCodes = customUserDetails.getEmployeeCodes();
                    Session session = entityManager.unwrap(Session.class);
                    Filter filter = session.enableFilter("auditableFilter");
                    filter.setParameterList("createdByList", createdByCodes);
                    filter.setParameterList("updatedByList", updatedByCodes);
                }
            }
        }
    }

    public void disable() {
        entityManager.unwrap(Session.class).disableFilter("auditableFilter");
    }
}
