package com.dat.erp.services.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dat.erp.entities.Account;
import com.dat.erp.entities.Department;
import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.services.CodeGenerator;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;

class AbstractAuditableServiceTest {

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private SecurityContextService securityContextService;

    private TestAuditableService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TestAuditableService();
        ReflectionTestUtils.setField(service, "codeGenerator", codeGenerator);
        ReflectionTestUtils.setField(service, "securityContextService", securityContextService);
    }

    @Test
    void generateCodeIfMissing_setsGeneratedCodeWhenBlank() {
        Department department = new Department();
        department.setCode("  ");
        when(codeGenerator.nextCode("DPM-")).thenReturn("DPM-000123");

        service.generateCodeIfMissing(department, "DPM-");

        assertThat(department.getCode()).isEqualTo("DPM-000123");
    }

    @Test
    void generateCodeIfMissing_doesNotOverrideExistingCode() {
        Department department = new Department();
        department.setCode("DPM-EXISTING");

        service.generateCodeIfMissing(department, "DPM-");

        assertThat(department.getCode()).isEqualTo("DPM-EXISTING");
    }

    @Test
    void applyInsertAudit_setsCreatedAndUpdatedFields_andCompanyCode() {
        Department department = new Department();

        Account account = new Account();
        account.setCode("ACC-1");
        account.setCompanyCode("CMP-1");
        when(securityContextService.getCurrentUser()).thenReturn(new CustomUserDetails(account, null));

        service.applyInsertAudit(department);

        assertThat(department.getCreatedBy()).isEqualTo("ACC-1");
        assertThat(department.getUpdatedBy()).isEqualTo("ACC-1");
        assertThat(department.getCompanyCode()).isEqualTo("CMP-1");
        assertThat(department.getCreatedAt()).isNotNull();
        assertThat(department.getUpdatedAt()).isNotNull();
        assertThat(department.getUpdatedAt()).isEqualTo(department.getCreatedAt());
    }

    @Test
    void applyInsertAudit_whenUnauthorized_setsSystemUser() {
        Department department = new Department();
        doThrow(new UnauthorizedException("no auth")).when(securityContextService).getCurrentUser();

        service.applyInsertAudit(department);

        assertThat(department.getCreatedBy()).isEqualTo("SYSTEM");
        assertThat(department.getUpdatedBy()).isEqualTo("SYSTEM");
        assertThat(department.getCompanyCode()).isEqualTo("SYSTEM");
    }

    static class TestAuditableService extends AbstractAuditableService {
    }
}

