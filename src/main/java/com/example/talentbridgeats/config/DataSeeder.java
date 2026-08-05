package com.example.talentbridgeats.config;

import com.example.talentbridgeats.model.Role;
import com.example.talentbridgeats.model.User;
import com.example.talentbridgeats.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("recruiter@talentbridge.com").isEmpty()) {
            User recruiter = User.builder()
                    .name("Default Recruiter")
                    .email("recruiter@talentbridge.com")
                    .password(passwordEncoder.encode("Recruiter@123"))
                    .role(Role.RECRUITER)
                    .build();
            userRepository.save(recruiter);
            System.out.println("Seeded recruiter account: recruiter@talentbridge.com / Recruiter@123");
        }
    }
}