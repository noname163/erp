package com.dat.erp.systemconfigs;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
                // Add your custom filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
