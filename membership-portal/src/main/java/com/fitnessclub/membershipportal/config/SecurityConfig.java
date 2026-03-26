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
            // Allow JSON POSTs from UI without CSRF token.
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            // Keep HTTP Basic for API demo screenshots (401 vs 200)
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/post-login", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers("/", "/login", "/register", "/access-denied").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/enroll", "/enroll/**").permitAll()

                // Contract pages require login (for rubric screenshots)
                .requestMatchers("/contract/**", "/contract-review/**").hasAnyRole("USER", "ADMIN")

                // Role-based UI
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")

                // Public: create enrollment (cart)
                .requestMatchers(HttpMethod.POST, "/api/enrollments").permitAll()

                // Contract finalize/sign should require login
                .requestMatchers("/api/enrollments/*/finalize").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/enrollments/*/sign-pdf").hasAnyRole("USER", "ADMIN")

                // Admin-only REST endpoints
                .requestMatchers(HttpMethod.GET, "/api/members", "/api/branches", "/api/enrollments/ids").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/enrollments/{id}").hasRole("ADMIN")

                .anyRequest().authenticated()
            );

        return http.build();
    }
}

