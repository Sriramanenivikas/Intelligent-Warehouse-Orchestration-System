package com.iwos.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token Provider
 * Handles JWT token generation, validation, and extraction
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${jwt.issuer}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
    }

    /**
     * Generate access token with username and roles
     */
    public String generateAccessToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .claim("roles", roles)
                .claim("type", "access")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate refresh token with username
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setId(UUID.randomUUID().toString())
                .claim("type", "refresh")
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extract username from JWT token
     */
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract roles from JWT token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = getClaims(token);
        return (List<String>) claims.get("roles");
    }

    /**
     * Extract token type (access or refresh)
     */
    public String extractTokenType(String token) {
        return (String) getClaims(token).get("type");
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            return getExpirationDate(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Get access token expiration time in seconds
     */
    public long getAccessTokenExpirationInSeconds() {
        return accessTokenExpiration / 1000;
    }

    /**
     * Get refresh token expiration time in seconds
     */
    public long getRefreshTokenExpirationInSeconds() {
        return refreshTokenExpiration / 1000;
    }

    /**
     * Get all claims from token
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract user ID from token if present
     */
    public Long extractUserId(String token) {
        Claims claims = getClaims(token);
        Object userId = claims.get("userId");
        if (userId != null) {
            return Long.parseLong(userId.toString());
        }
        return null;
    }
}
