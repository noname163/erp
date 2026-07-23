package com.dat.erp.aspsect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.dat.erp.systemconfigs.AuditableFilterManager;

@Aspect
@Component
public class AuditableFilterAspect {

    private final AuditableFilterManager filterManager;

    public AuditableFilterAspect(AuditableFilterManager filterManager) {
        this.filterManager = filterManager;
    }

    @Before("@within(org.springframework.stereotype.Service)")
    public void enableFilterBeforeServiceMethod() {
        filterManager.enable();
    }
}
