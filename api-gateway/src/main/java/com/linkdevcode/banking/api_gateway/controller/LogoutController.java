package com.linkdevcode.banking.api_gateway.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    private final ReactiveStringRedisTemplate redis;

    public LogoutController(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(
            @AuthenticationPrincipal Jwt jwt) {

        String jti = jwt.getId();
        Instant exp = jwt.getExpiresAt();

        long ttl = Duration.between(Instant.now(), exp).getSeconds();

        if (ttl <= 0) {
            return Mono.just(ResponseEntity.ok().build());
        }

        return redis.opsForValue()
                .set("blacklist:" + jti, "1", Duration.ofSeconds(ttl))
                .thenReturn(ResponseEntity.ok().build());
    }
}
