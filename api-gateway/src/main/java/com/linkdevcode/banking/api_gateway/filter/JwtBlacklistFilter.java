package com.linkdevcode.banking.api_gateway.filter;

import org.springframework.stereotype.Component;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpStatus;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import reactor.core.publisher.Mono;

@Component
public class JwtBlacklistFilter implements GlobalFilter {

    private final ReactiveStringRedisTemplate redis;

    public JwtBlacklistFilter(ReactiveStringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        return exchange.getPrincipal()
            .cast(JwtAuthenticationToken.class)
            .flatMap(auth -> {
                String jti = auth.getToken().getId();

                return redis.hasKey("blacklist:" + jti)
                    .flatMap(isBlacklisted -> {
                        if (isBlacklisted) {
                            exchange.getResponse()
                                .setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    });
            })
            .switchIfEmpty(chain.filter(exchange));
    }
}