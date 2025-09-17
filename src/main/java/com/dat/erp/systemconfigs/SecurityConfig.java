package com.dat.erp.systemconfigs;

import com.dat.erp.filters.AuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;

    public SecurityConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disable CSRF if using JWT/cookies
                .authorizeHttpRequests(auth -> auth
                        // ✅ Whitelist URLs
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/public/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()

                        // everything else requires authentication
                        .anyRequest().authenticated())
                // Add your custom filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
