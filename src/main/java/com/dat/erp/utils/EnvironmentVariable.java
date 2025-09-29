package com.dat.erp.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

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

    public List<String> getWhitelistAsList() {
        return Arrays.asList(whitelist.split(","));
    }
}
