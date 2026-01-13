package com.linkdevcode.banking.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.linkdevcode.banking.user_service.entity.User;

import java.util.Optional;

public interface UserRepository 
        extends JpaRepository<User, Long>, 
                JpaSpecificationExecutor<User> {

    // For Login/Authentication
    Optional<User> findByUsername(String username);

    // For Password Reset
    Optional<User> findByEmail(String email);

    // For Register/Validation
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}