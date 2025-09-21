package com.dat.erp.systemconfigs;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.dat.erp.services.SecurityContextService;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<String> auditorProvider(SecurityContextService securityContextService) {
        return () -> Optional.ofNullable(securityContextService.getCurrentUser().getCode());
    }
}
