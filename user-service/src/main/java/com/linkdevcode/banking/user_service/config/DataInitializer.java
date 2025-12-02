package com.linkdevcode.banking.user_service.config;

import com.linkdevcode.banking.user_service.model.ERole;
import com.linkdevcode.banking.user_service.model.Role;
import com.linkdevcode.banking.user_service.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

/**
 * Configuration class to initialize essential data upon application startup.
 * Specifically handles the creation of default user roles if they don't exist in the database.
 */
@Configuration
public class DataInitializer {

    /**
     * Creates a CommandLineRunner bean responsible for initializing default roles in the database.
     * This ensures all roles defined in the {@code ERole} enum are present upon application start.
     */
    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            // Iterate over all possible role names defined in the ERole enum.
            Arrays.stream(ERole.values()).forEach(roleName -> {
                // Check if the role already exists in the database by searching its name.
                if (roleRepository.findByName(roleName).isEmpty()) {
                    // Role doesn't exist, create a new Role entity.
                    Role role = new Role();
                    role.setName(roleName);

                    // Save the new role to the database.
                    roleRepository.save(role);

                    // Log the initialization for debugging/monitoring purposes.
                    System.out.println("Initialized role: " + roleName);
                }
            });
        };
    }
}