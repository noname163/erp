package com.dat.erp.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.dat.erp.constants.Messages;
import com.dat.erp.dto.response.PagedResponse;
import com.dat.erp.dto.response.PayrollResultListResponse;
import com.dat.erp.filters.AuthenticationFilter;
import com.dat.erp.filters.RequestIdFilter;
import com.dat.erp.services.payroll.PayrollResultService;
import com.dat.erp.systemconfigs.SecurityConfig;
import com.dat.erp.utils.EnvironmentVariable;

@WebMvcTest(PayrollResultController.class)
@Import(SecurityConfig.class)
class PayrollResultControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollResultService payrollResultService;

    @MockBean
    private AuthenticationFilter authenticationFilter;

    @MockBean
    private RequestIdFilter requestIdFilter;

    @MockBean
    private EnvironmentVariable environmentVariable;

    @BeforeEach
    void setUp() throws Exception {
        when(environmentVariable.getWhitelistAsList()).thenReturn(List.of("/api/auth/**"));
        when(environmentVariable.getCorsAllowedOriginsAsList()).thenReturn(List.of("http://localhost"));
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain filterChain = invocation.getArgument(2);
            try {
                filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            } catch (jakarta.servlet.ServletException | java.io.IOException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        }).when(authenticationFilter).doFilter(any(), any(), any());
        doAnswer(invocation -> {
            jakarta.servlet.FilterChain filterChain = invocation.getArgument(2);
            try {
                filterChain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            } catch (jakarta.servlet.ServletException | java.io.IOException ex) {
                throw new RuntimeException(ex);
            }
            return null;
        }).when(requestIdFilter).doFilter(any(), any(), any());
        when(payrollResultService.getPayrollResults(
                eq("PRN-1"),
                eq(LocalDate.of(2026, 4, 1)),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                eq("DESC")))
                        .thenReturn(PagedResponse.<PayrollResultListResponse>builder()
                                .data(List.of())
                                .page(0)
                                .size(20)
                                .totalElements(0)
                                .totalPages(0)
                                .last(true)
                                .message(Messages.SUCCESS)
                                .success(true)
                                .build());
    }

    @Test
    void getPayrollResults_employeeAuthorityIsAllowed() throws Exception {
        mockMvc.perform(get("/api/payroll-results")
                .param("payrollRunCode", "PRN-1")
                .param("createdDate", "2026-04-01")
                .with(user("employee").authorities(() -> "EMPLOYEE")))
                .andExpect(status().isOk());

        verify(payrollResultService).getPayrollResults("PRN-1", LocalDate.of(2026, 4, 1), null, null, null, null, null,
                "DESC");
    }

    // @Test
    // void getPayrollResults_managerAuthorityIsRejected() throws Exception {
    //     mockMvc.perform(get("/api/payroll-results")
    //             .param("payrollRunCode", "PRN-1")
    //             .param("createdDate", "2026-04-01")
    //             .with(user("manager").authorities(() -> "MANAGER")))
    //             .andExpect(status().isForbidden());
    // }
}
