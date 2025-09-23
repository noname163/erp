package com.dat.erp.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    private final EnvironmentVariable environmentVariable;
    private final Key key;

    public JwtUtils(EnvironmentVariable environmentVariable) {
        this.environmentVariable = environmentVariable;
        this.key = Keys.hmacShaKeyFor(environmentVariable.getJwtSecret().getBytes());
    }

    /**
     * Generate JWT token for a given username
     */
    public String generateToken(String employeeName, String employeeCode) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + environmentVariable.getJwtExpirationMs());
        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeCode", employeeCode);
        claims.put("employeeName", employeeName);
        return Jwts.builder().issuedAt(expiryDate).claims(claims).signWith(key).compact();
    }

    public String extractEmployeeName(String token) {
        return extractClaim(token, claims -> claims.get("employeeName", String.class));
    }

    /**
     * Extract employeeCode from JWT
     */
    public String extractEmployeeCode(String token) {
        return extractClaim(token, claims -> claims.get("employeeCode", String.class));
    }

    /**
     * Extract expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.get("iat", Date.class));
    }

    /**
     * Validate token
     */
    public boolean validateToken(String token, String expectedEmployeeCode) {
        try {
            String employeeCode = extractEmployeeCode(token);
            return (employeeCode.equals(expectedEmployeeCode) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract a claim using a resolver function
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse all claims safely
     */
    private Claims parseClaims(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(environmentVariable.getJwtSecret().getBytes());
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
