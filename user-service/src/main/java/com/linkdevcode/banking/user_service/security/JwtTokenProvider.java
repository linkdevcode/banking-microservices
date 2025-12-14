package com.linkdevcode.banking.user_service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private int jwtExpirationMs;

    private Key key() {
        // Use HS512 for signing the token
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate JWT Token
    */
    public String generateJwtToken(Authentication authentication) {

        Instant now = Instant.now();

        return Jwts.builder()
            .setSubject(authentication.getName())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(jwtExpirationMs, ChronoUnit.MILLIS)))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Get username from JWT Token
    */
    public String getUserNameFromJwtToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get issued at date from JWT Token
    */
    public Instant getIssuedAtFromJwtToken(String token) {
        return getClaims(token).getIssuedAt().toInstant();
    }

    /**
     * Get claims from JWT Token
    */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
    }

    /**
     * Validate JWT Token
    */
    public Jws<Claims> validateJwtToken(String authToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
            throw new JwtException("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
            throw new JwtException("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
            throw new JwtException("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
            throw new JwtException("JWT claims string is empty.");
        }
    }
}