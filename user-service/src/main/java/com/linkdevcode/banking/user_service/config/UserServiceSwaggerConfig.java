package com.linkdevcode.banking.user_service.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class UserServiceSwaggerConfig {

    @Bean
    public OpenApiCustomizer gatewayServerCustomizer() {
        return openApi -> {
            // Generate a Server object pointing to the API Gateway
            Server gatewayServer = new Server();
            gatewayServer.setUrl("http://localhost:8000/api/user");
            gatewayServer.setDescription("API Gateway Entry Point for User Service");

            // Set this server in the OpenAPI definition
            openApi.setServers(List.of(gatewayServer));
        };
    }
}