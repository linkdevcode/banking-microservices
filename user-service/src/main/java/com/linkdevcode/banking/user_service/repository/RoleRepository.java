package com.linkdevcode.banking.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkdevcode.banking.user_service.enumeration.ERole;
import com.linkdevcode.banking.user_service.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    // Find role by its name
    Optional<Role> findByName(ERole name);
}