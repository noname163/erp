package com.dat.erp.filters;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dat.erp.contexts.TenantContext;
import com.dat.erp.services.SecurityContextService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TenantContextFilter extends OncePerRequestFilter {

    private final SecurityContextService securityContextService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            var currentUser = securityContextService.getCurrentUser();

            if (currentUser != null
                    && currentUser.getAccount() != null) {
                TenantContext.setCompanyCode(
                        currentUser.getAccount().getCompanyCode());
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
