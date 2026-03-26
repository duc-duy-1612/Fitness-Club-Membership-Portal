package com.fitnessclub.membershipportal.repository;

import com.fitnessclub.membershipportal.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByMember_Id(Integer memberId);

    boolean existsByUsername(String username);
}

