package com.dat.erp.systemconfigs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dat.erp.filters.AuthenticationFilter;
import com.dat.erp.utils.EnvironmentVariable;

@Configuration
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;
    private final EnvironmentVariable environmentVariable;

    public SecurityConfig(AuthenticationFilter authenticationFilter,
            EnvironmentVariable environmentVariable) {
        this.authenticationFilter = authenticationFilter;
        this.environmentVariable = environmentVariable;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ whitelist from EnvironmentVariable
                        .requestMatchers(environmentVariable.getWhitelistAsList().toArray(new String[0]))
                        .permitAll()
                        // everything else requires authentication
                        .anyRequest().authenticated())
                // Add your custom filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
