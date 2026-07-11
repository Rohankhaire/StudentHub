package com.studenthub.config;

import com.studenthub.entity.Role;
import com.studenthub.entity.User;
import com.studenthub.entity.UserStatus;
import com.studenthub.repository.RoleRepository;
import com.studenthub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Ensure roles exist
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_ADMIN")));
        roleRepository.findByName("ROLE_FACULTY")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_FACULTY")));
        roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_STUDENT")));

        // Create default admin user if no admin exists in the database
        if (userRepository.findByEmail("admin@studenthub.com").isEmpty() &&
            userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            
            User admin = User.builder()
                    .email("admin@studenthub.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .phone("1234567890")
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Collections.singletonList(adminRole)))
                    .build();
            
            userRepository.save(admin);
            System.out.println("Default Admin user created: admin@studenthub.com / admin123");
        }
    }
}
