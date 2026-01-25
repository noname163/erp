package com.dat.erp.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
public class EnvironmentVariable {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    @Value("${security.whitelist}")
    private String whitelist; // raw string from properties

    @Value("${cors.allowed-origins:}")
    private String corsAllowedOrigins; // raw string from properties/env

    public List<String> getWhitelistAsList() {
        return Arrays.asList(whitelist.split(","));
    }

    public List<String> getCorsAllowedOriginsAsList() {
        if (corsAllowedOrigins == null || corsAllowedOrigins.isBlank()) {
            return List.of();
        }
        return Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
