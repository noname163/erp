package com.dat.erp.filters;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dat.erp.services.SecurityContextService;
import com.dat.erp.systemconfigs.CustomUserDetails;
import com.dat.erp.utils.JwtUtils;
import com.dat.erp.utils.PermissionUtils;

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

        // 4. If no token → just continue (but not authenticated)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extract employeeCode (subject)
        String employeeCode = jwtUtils.extractEmployeeCode(token);
        CustomUserDetails customUserDetails = null;
        // 6. Authenticate if not already set
        if (employeeCode != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtils.validateToken(token, employeeCode)) {
                customUserDetails = securityContextService.setCurrentUser(employeeCode);
            }
        }

        String url = request.getRequestURI();
        String method = request.getMethod();
        if (customUserDetails != null
                && PermissionUtils.hasPermission(customUserDetails.getPermissionMap(), url, method)) {
            filterChain.doFilter(request, response);
            customUserDetails.setViewAll(PermissionUtils.hasViewAllPermission(
                    customUserDetails.getPermissionMap(), url));
            customUserDetails.setViewOwnedOnly(PermissionUtils.hasViewAllPermission(
                    customUserDetails.getPermissionMap(), url));
        } else {
            log.error("User {} not have permission {} on URL {} ", customUserDetails.getCode(), method, url);
            throw new AccessDeniedException("User not have permission");
        }

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
