package com.dat.erp.filters;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.EnvironmentVariable;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final EnvironmentVariable environmentVariable;
    private final SecurityContextService securityContextService;

    public AuthenticationFilter(JwtUtils jwtUtils, EnvironmentVariable environmentVariable,
            SecurityContextService securityContextService) {
        this.jwtUtils = jwtUtils;
        this.environmentVariable = environmentVariable;
        this.securityContextService = securityContextService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        return environmentVariable.getWhitelistAsList().stream()
                .anyMatch(pattern -> matcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractTokenFromCookies(request);

        if (token != null) {
            String accountCode = jwtUtils.extractAccountCode(token);
            if (accountCode != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtils.validateToken(token, accountCode)) {
                    securityContextService.setCurrentUser(accountCode);
                    log.debug("Authenticated account {}", accountCode);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
