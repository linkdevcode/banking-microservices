package com.linkdevcode.banking.user_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public String generateToken(
            Long userId,
            String username,
            String email,
            List<String> roles
    ) {

        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("email", email)
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
