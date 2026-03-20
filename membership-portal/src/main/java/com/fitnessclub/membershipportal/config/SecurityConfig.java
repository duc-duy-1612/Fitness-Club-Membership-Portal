package com.fitnessclub.membershipportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.http.HttpMethod;

import com.fitnessclub.membershipportal.entity.UserAccount;
import com.fitnessclub.membershipportal.repository.UserAccountRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountRepository userAccountRepository) {
        return username -> {
            UserAccount ua = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            // Spring Security expects role with "ROLE_" prefix for hasRole().
            String role = ua.getRole() != null ? ua.getRole() : "ADMIN";
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

            UserDetails ud = User.withUsername(ua.getUsername())
                    .password(ua.getPassword())
                    .roles(authority.startsWith("ROLE_") ? authority.substring("ROLE_".length()) : authority)
                    .build();
            return ud;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Allow browser/PDF actions from UI without CSRF token.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            // Use HTTP Basic so API unauth requests return 401 (easy to screenshot).
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/", "/enroll", "/contract/**", "/contract-review/**", "/css/**").permitAll()

                // Allow signup/cart create
                .requestMatchers(HttpMethod.POST, "/api/enrollments").permitAll()

                // Allow PDF signing + downloading endpoints (so the UI works without login)
                .requestMatchers(HttpMethod.POST, "/api/enrollments/*/sign-pdf").permitAll()
                .requestMatchers("/api/enrollments/*/finalize").permitAll()

                // Protected REST endpoints (required for "Unauthorized access" security screenshots)
                .requestMatchers(HttpMethod.GET, "/api/members", "/api/branches", "/api/enrollments/ids").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/enrollments/{id}").authenticated()

                .anyRequest().permitAll()
            );

        return http.build();
    }
}

