package com.dat.erp.services.base;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;

import com.dat.erp.entities.BaseAuditableEntity;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;

public abstract class AbstractAuditableService {

    private static final String SYSTEM_USER = "SYSTEM";

    @Autowired
    protected CodeGenerator codeGenerator;

    @Autowired
    protected SecurityContextService securityContextService;

    protected String generateCode(String prefix) {
        return codeGenerator.nextCode(prefix);
    }

    protected void generateCodeIfMissing(BaseAuditableEntity entity, String prefix) {
        if (entity.getCode() == null || entity.getCode().isBlank()) {
            entity.setCode(generateCode(prefix));
        }
    }

    protected void applyInsertAudit(BaseAuditableEntity entity) {
        String userCode = resolveCurrentUserCode();
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        if (entity.getCreatedBy() == null || entity.getCreatedBy().isBlank()) {
            entity.setCreatedBy(userCode);
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(nowUtc);
        }

        entity.setUpdatedBy(userCode);
        entity.setUpdatedAt(nowUtc);
        if (entity.getCompanyCode() == null || entity.getCompanyCode().isBlank()) {
            entity.setCompanyCode(resolveCurrentUserCompanyCode());
        }
    }

    protected void applyUpdateAudit(BaseAuditableEntity entity) {
        String userCode = resolveCurrentUserCode();
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        entity.setUpdatedBy(userCode);
        entity.setUpdatedAt(nowUtc);
    }

    private String resolveCurrentUserCode() {
        try {
            if (securityContextService.getCurrentUser() == null) {
                return SYSTEM_USER;
            }
            String code = securityContextService.getCurrentUser().getCode();
            return (code == null || code.isBlank()) ? SYSTEM_USER : code;
        } catch (UnauthorizedException ex) {
            return SYSTEM_USER;
        }
    }

    public String resolveCurrentUserCompanyCode() {
        try {
            if (securityContextService.getCurrentUser() == null) {
                return SYSTEM_USER;
            }
            String companyCode = securityContextService.getCurrentUser().getAccount().getCompanyCode();
            return (companyCode == null || companyCode.isBlank()) ? SYSTEM_USER : companyCode;
        } catch (UnauthorizedException ex) {
            return SYSTEM_USER;
        }
    }
}
