package com.linkdevcode.banking.user_service.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
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
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get username from JWT Token
    */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
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