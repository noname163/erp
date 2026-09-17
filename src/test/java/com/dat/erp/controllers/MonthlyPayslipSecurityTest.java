package com.dat.erp.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import com.dat.erp.filters.*;
import com.dat.erp.repositories.customrepositories.PayrollResultRepository;
import com.dat.erp.services.SecurityContextService;
import com.dat.erp.services.payroll.PayrollInputService;
import com.dat.erp.systemconfigs.SecurityConfig;
import com.dat.erp.utils.EnvironmentVariable;

@WebMvcTest(MonthlyPayslipController.class)
@Import(SecurityConfig.class)
class MonthlyPayslipSecurityTest {
    @Autowired MockMvc mvc;
    @MockBean PayrollResultRepository results;
    @MockBean SecurityContextService security;
    @MockBean PayrollInputService inputs;
    @MockBean AuthenticationFilter authenticationFilter;
    @MockBean RequestIdFilter requestIdFilter;
    @MockBean TenantContextFilter tenantContextFilter;
    @MockBean EnvironmentVariable environment;
    @BeforeEach void setup() throws Exception {
        when(environment.getWhitelistAsList()).thenReturn(List.of("/api/auth/**"));
        when(environment.getCorsAllowedOriginsAsList()).thenReturn(List.of("http://localhost"));
        org.mockito.stubbing.Answer<Void> pass = invocation -> {
            jakarta.servlet.FilterChain chain=invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0),invocation.getArgument(1));return null;
        };
        doAnswer(pass).when(authenticationFilter).doFilter(any(),any(),any());
        doAnswer(pass).when(requestIdFilter).doFilter(any(),any(),any());
        doAnswer(pass).when(tenantContextFilter).doFilter(any(),any(),any());
    }
    @Test void employeeCannotReadOtherPayrollOrConfigureInputs() throws Exception {
        mvc.perform(get("/api/payroll-payslips/RESULT").with(user("employee").authorities(()->"EMPLOYEE"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/payroll-payslips/RESULT/configuration").contentType("application/json").content("{}")
            .with(user("employee").authorities(()->"EMPLOYEE"))).andExpect(status().isForbidden());
        verifyNoInteractions(results,inputs);
    }
    @Test void hrReadIsScopedToCurrentCompany() throws Exception {
        when(security.getCurrentCompanyCode()).thenReturn("COMPANY-A");
        when(results.findByCodeAndCompanyCodeAndIsDeletedFalse("FOREIGN-RESULT","COMPANY-A")).thenReturn(Optional.empty());
        mvc.perform(get("/api/payroll-payslips/FOREIGN-RESULT").with(user("hr").authorities(()->"HUMAN_RESOURCES"))).andExpect(status().isNotFound());
        verify(results).findByCodeAndCompanyCodeAndIsDeletedFalse("FOREIGN-RESULT","COMPANY-A");
        verifyNoInteractions(inputs);
    }
    @Test void unauthenticatedReadIsRejected() throws Exception {
        mvc.perform(get("/api/payroll-payslips/RESULT")).andExpect(status().isUnauthorized());
    }

    @Test void companyManagerCanReachPayrollConfigurationValidation() throws Exception {
        mvc.perform(post("/api/payroll-payslips/RESULT/configuration")
                .contentType("application/json")
                .content("{}")
                .with(user("manager").authorities(() -> "COMPANY_MANAGER")))
                .andExpect(status().isBadRequest());
    }
}
