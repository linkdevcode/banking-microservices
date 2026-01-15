package com.linkdevcode.banking.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
            // Public
            .pathMatchers(
                "/api/auth/register",
                "/api/auth/login",
                "/api/auth/forgot-password",
                "/api/auth/reset-password",
                "/v3/api-docs/**",
                "/swagger-ui/**"
            ).permitAll()

            // Protected
            .pathMatchers(
                "/api/auth/logout",
                "/api/auth/change-password",
                "/api/users/me",
                "/api/account/get-balance",
                "/api/payment/**",
                "/api/history/search"
            ).authenticated()

            .pathMatchers(
                "/api/users/search",
                "/api/admin/history/search",
                "/api/batch/**"
            ).hasAuthority("ROLE_ADMIN")

            // Other
            .anyExchange().denyAll()
        )
        .oauth2ResourceServer(oauth -> oauth
            .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
        )
        .build();
    }

    Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        authoritiesConverter.setAuthoritiesClaimName("roles"); 
        
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        
        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}