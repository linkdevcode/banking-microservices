package com.linkdevcode.banking.user_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Tên Scheme được dùng để tham chiếu (ví dụ: trong @SecurityRequirement)
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP) // Loại bảo mật: HTTP
                                        .scheme("bearer")               // Scheme là bearer (dùng cho JWT)
                                        .bearerFormat("JWT")            // Định dạng token
                                        .description("Enter JWT Bearer token only")
                        )
                );
    }
}