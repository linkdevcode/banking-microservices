package com.linkdevcode.banking.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Lazy; // Use @Lazy for reactive context

import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class SwaggerConfig {

    // Helper class to hold the OpenAPI resources (paths to YAML/JSON)
    private final RouteDefinitionLocator locator;

    public SwaggerConfig(RouteDefinitionLocator locator) {
        this.locator = locator;
    }

    // Bean to generate the aggregated OpenAPI documentation list
    // This allows Swagger UI to combine documentation from all microservices.
    @Bean
    @Primary
    @Lazy // Important for reactive application startup
    public List<GroupedOpenApi> apis(SwaggerUiConfigParameters swaggerUiConfigParameters) {
        List<GroupedOpenApi> groups = new ArrayList<>();
        List<String> routeIds = new ArrayList<>();

        // Get all route definitions configured in the gateway (from application.yml/Eureka)
        locator.getRouteDefinitions().subscribe(routeDefinition -> routeIds.add(routeDefinition.getId()));

        // Filter and map the discovered route IDs to GroupedOpenApi objects
        routeIds.stream()
                // Only consider routes that are not the gateway's internal docs
                .filter(id -> id.startsWith("user_service")) // Filter for specific service routes
                .forEach(id -> {
                    // Remove the '_route' suffix for cleaner group names
                    String groupName = id.replace("_route", "");

                    // Add the GroupedOpenApi definition
                    groups.add(GroupedOpenApi.builder()
                            .pathsToMatch("/api/" + groupName + "/**") // Paths that this group covers
                            .group(groupName) // Group name shown in Swagger UI
                            .build());
                });

        // Add the generated group names to Swagger UI parameters for display
        // Create SwaggerUrl objects for each group
        java.util.Set<org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls =
                groups.stream()
                        .map(group -> {
                            org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl url =
                                    new org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl();
                            url.setName(group.getGroup());
                            url.setUrl("/v3/api-docs/" + group.getGroup());
                            return url;
                        })
                        .collect(java.util.stream.Collectors.toSet());
        swaggerUiConfigParameters.setUrls(swaggerUrls);

        return groups;
    }

    // Customization for the main API Gateway documentation info
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authorization required for most endpoints.")
                        )
                )
                .info(new Info().title("Banking Microservices API Gateway")
                        .version("1.0")
                        .description("Aggregated documentation for all backend services."));
    }
}