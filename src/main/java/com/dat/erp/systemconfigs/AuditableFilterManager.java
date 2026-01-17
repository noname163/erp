package com.dat.erp.systemconfigs;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
public class AuditableFilterManager {

    private final EntityManager entityManager;

    public AuditableFilterManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void enable() {
        // Auditable filter is currently disabled in the simplified security model.
    }

    public void disable() {
        entityManager.unwrap(Session.class).disableFilter("auditableFilter");
    }
}
