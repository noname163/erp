package com.dat.erp.filters;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final SecurityContextService securityContextService;

    public AuthenticationFilter(JwtUtils jwtUtils,
            SecurityContextService securityContextService) {
        this.jwtUtils = jwtUtils;
        this.securityContextService = securityContextService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // 2. Try to get token from Cookie first
        String token = extractTokenFromCookies(request);

        // 3. Fallback: Authorization header
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        // 4. If no token → just continue (but not authenticated)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extract employeeCode (subject)
        String employeeCode = jwtUtils.extractEmployeeCode(token);

        // 6. Authenticate if not already set
        if (employeeCode != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtils.validateToken(token, employeeCode)) {
                securityContextService.setCurrentUser(employeeCode);
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
