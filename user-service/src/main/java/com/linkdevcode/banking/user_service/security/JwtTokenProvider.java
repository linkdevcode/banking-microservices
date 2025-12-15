package com.linkdevcode.banking.user_service.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.linkdevcode.banking.user_service.service.UserDetailsImpl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final JwtKeyProvider keyProvider;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    public JwtTokenProvider(JwtKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        Instant now = Instant.now();

        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .setId(UUID.randomUUID().toString()) // jti
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(jwtExpirationMs)))
                .setIssuer("user-service")
                .setAudience("api-gateway")
                .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }
}