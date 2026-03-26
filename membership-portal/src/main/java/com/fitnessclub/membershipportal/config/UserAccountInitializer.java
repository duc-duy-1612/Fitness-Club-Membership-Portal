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

        // 1) Ensure NEW admin account: admin@gmail.com / admin123
        String adminUsername = "admin@gmail.com";
        UserAccount admin = userAccountRepository.findByUsername(adminUsername).orElse(null);
        if (admin == null) {
            admin = new UserAccount(adminUsername, encoded, "ADMIN");
        } else {
            // Overwrite every startup so the demo credentials always work.
            admin.setPassword(encoded);
            admin.setRole("ADMIN");
        }
        userAccountRepository.save(admin);

        // 2) Optional: demote old legacy admin username "admin" to USER (so it won't grant ADMIN UI).
        //    If the old row doesn't exist, ignore.
        String legacyUsername = "admin";
        userAccountRepository.findByUsername(legacyUsername).ifPresent(legacy -> {
            legacy.setRole("USER");
            userAccountRepository.save(legacy);
        });
    }
}

