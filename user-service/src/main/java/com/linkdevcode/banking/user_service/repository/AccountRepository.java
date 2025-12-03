package com.linkdevcode.banking.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.linkdevcode.banking.user_service.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
}
