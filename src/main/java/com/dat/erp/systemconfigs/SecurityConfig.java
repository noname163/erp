package com.dat.erp.systemconfigs;

import java.util.List;

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
import com.dat.erp.utils.EnvironmentVariable;

@Configuration
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;
    private final EnvironmentVariable environmentVariable;
    private final RequestIdFilter requestIdFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter,
            EnvironmentVariable environmentVariable,
            RequestIdFilter requestIdFilter) {
        this.authenticationFilter = authenticationFilter;
        this.environmentVariable = environmentVariable;
        this.requestIdFilter = requestIdFilter;
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
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
