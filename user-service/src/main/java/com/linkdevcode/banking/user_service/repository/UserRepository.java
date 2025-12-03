package com.linkdevcode.banking.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.linkdevcode.banking.user_service.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // For Login/Authentication
    Optional<User> findByUsername(String username);

    // For Register/Validation
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    // For Search User (Pagination) - Task 2.4
    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
}