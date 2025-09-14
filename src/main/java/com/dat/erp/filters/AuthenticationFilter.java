package com.dat.erp.filters;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.dat.erp.services.SecurityContextService;
import com.dat.erp.utils.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final SecurityContextService securityContextService;

    public AuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService,
            SecurityContextService securityContextService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.securityContextService = securityContextService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract JWT token
        String token = authHeader.substring(7);

        // 3. Extract employeeCode (subject)
        String employeeCode = jwtUtils.extractEmployeeCode(token);

        // 4. Authenticate if not already set
        if (employeeCode != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtils.validateToken(token, employeeCode)) {
                securityContextService.setCurrentUser(employeeCode);
            }
        }

        filterChain.doFilter(request, response);
    }

}
