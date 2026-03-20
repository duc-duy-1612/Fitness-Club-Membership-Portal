package com.fitnessclub.membershipportal.config;

import com.fitnessclub.membershipportal.entity.UserAccount;
import com.fitnessclub.membershipportal.repository.UserAccountRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seed an initial admin user (password is stored as BCrypt hash in DB).
 */
@Component
public class UserAccountInitializer {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountInitializer(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // Always ensure the admin user exists and has a BCrypt password hash.
        String rawPassword = "admin123";
        String encoded = passwordEncoder.encode(rawPassword);

        UserAccount admin = userAccountRepository.findByUsername("admin").orElse(null);
        if (admin == null) {
            userAccountRepository.save(new UserAccount("admin", encoded, "ADMIN"));
            return;
        }

        // If SQL seed created a plain-text password (or any other format), overwrite it.
        if (admin.getPassword() == null || !admin.getPassword().startsWith("$2")) {
            admin.setPassword(encoded);
            admin.setRole("ADMIN");
            userAccountRepository.save(admin);
        }
    }
}

