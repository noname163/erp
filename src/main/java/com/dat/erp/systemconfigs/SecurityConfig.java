package com.dat.erp.systemconfigs;

import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.dat.erp.filters.AuthenticationFilter;
import com.dat.erp.filters.RequestIdFilter;
import com.dat.erp.filters.TenantContextFilter;
import com.dat.erp.utils.EnvironmentVariable;

@Configuration
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;
    private final EnvironmentVariable environmentVariable;
    private final RequestIdFilter requestIdFilter;
    private final TenantContextFilter tenantContextFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter,
            EnvironmentVariable environmentVariable,
            RequestIdFilter requestIdFilter,
            TenantContextFilter tenantContextFilter) {
        this.authenticationFilter = authenticationFilter;
        this.environmentVariable = environmentVariable;
        this.requestIdFilter = requestIdFilter;
        this.tenantContextFilter = tenantContextFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        // ✅ whitelist from EnvironmentVariable
                        .requestMatchers(environmentVariable.getWhitelistAsList().toArray(new String[0]))
                        .permitAll()
                        // everything else requires authentication
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                // Add your custom filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(authenticationFilter, RequestIdFilter.class)
                .addFilterAfter(tenantContextFilter, AuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration(RequestIdFilter filter) {
        return disabledRegistration(filter);
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistration(AuthenticationFilter filter) {
        return disabledRegistration(filter);
    }

    @Bean
    public FilterRegistrationBean<TenantContextFilter> tenantContextFilterRegistration(TenantContextFilter filter) {
        return disabledRegistration(filter);
    }

    private <T extends jakarta.servlet.Filter> FilterRegistrationBean<T> disabledRegistration(T filter) {
        FilterRegistrationBean<T> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"type\":\"https://example.com/errors/unauthorized\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"AUTH_401_001: Unauthorized\",\"instance\":\""
                            + request.getRequestURI() + "\"}");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"type\":\"https://example.com/errors/forbidden\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"AUTH_403_001: Forbidden\",\"instance\":\""
                            + request.getRequestURI() + "\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(environmentVariable.getCorsAllowedOriginsAsList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
