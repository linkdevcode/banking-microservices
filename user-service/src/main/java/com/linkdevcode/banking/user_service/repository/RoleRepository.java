package com.linkdevcode.banking.user_service.repository;

import com.linkdevcode.banking.user_service.model.ERole;
import com.linkdevcode.banking.user_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}