package com.linkdevcode.banking.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.linkdevcode.banking.user_service.entity.PasswordResetToken;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /** Finds a token by its unique string value. */
    Optional<PasswordResetToken> findByToken(String token);
    
    /** Deletes all tokens associated with a specific user ID. */
    void deleteByUserId(Long userId); // Spring Data JPA automatically generates implementation for this custom query.
}