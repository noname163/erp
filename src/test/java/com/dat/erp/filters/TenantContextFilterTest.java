package com.dat.erp.filters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.dat.erp.contexts.TenantContext;

class TenantContextFilterTest {

    private final TenantContextFilter filter = new TenantContextFilter();

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void anonymousLoginRequestContinuesWithoutTenant() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        boolean[] chainInvoked = { false };

        assertDoesNotThrow(() -> filter.doFilter(
                request,
                new MockHttpServletResponse(),
                (servletRequest, servletResponse) -> chainInvoked[0] = true));

        assertTrue(chainInvoked[0]);
        assertThrows(IllegalStateException.class, TenantContext::requireCompanyCode);
    }
}
