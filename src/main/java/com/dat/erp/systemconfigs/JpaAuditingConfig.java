package com.dat.erp.systemconfigs;

import java.util.Optional;

import org.springframework.data.auditing.DateTimeProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.dat.erp.exceptions.UnauthorizedException;
import com.dat.erp.services.SecurityContextService;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<String> auditorProvider(SecurityContextService securityContextService) {
        return () -> {
            try {
                return Optional.ofNullable(securityContextService.getCurrentUser())
                        .map(user -> user.getCode())
                        .or(() -> Optional.of("SYSTEM"));
            } catch (UnauthorizedException ex) {
                return Optional.of("SYSTEM");
            }
        };
    }

    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(java.time.LocalDateTime.now(java.time.Clock.systemUTC()));
    }
}
