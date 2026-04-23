package com.example.bookstore.config;

import com.example.bookstore.entity.Role;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@bookstore.com")) {
                userRepository.save(User.builder()
                        .fullName("Bookstore Admin")
                        .email("admin@bookstore.com")
                        .password(passwordEncoder.encode("Admin123*"))
                        .role(Role.ROLE_ADMIN)
                        .build());
            }
        };
    }
}
