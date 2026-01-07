package com.linkdevcode.banking.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {

        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            .authorizeExchange(auth -> auth

                // ====== SWAGGER & DOCS ======
                .pathMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // ====== AUTH APIs ======
                .pathMatchers("/api/auth/**").permitAll()

                // ====== CORS PREFLIGHT ======
                .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**")
                .permitAll()

                // ====== OTHERS ======
                .anyExchange().permitAll()
            );
            
        return http.build();
    }
}