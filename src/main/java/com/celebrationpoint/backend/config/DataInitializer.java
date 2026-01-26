package com.celebrationpoint.backend.config;

import com.celebrationpoint.backend.constants.RoleType;
import com.celebrationpoint.backend.entity.Role;
import com.celebrationpoint.backend.entity.User;
import com.celebrationpoint.backend.repository.RoleRepository;
import com.celebrationpoint.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Set;

@Component
public class DataInitializer {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {

        // ===============================
        // ✅ CREATE ROLES
        // ===============================
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_ADMIN)));

        roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_USER)));

        // ===============================
        // ✅ CREATE DEFAULT ADMIN
        // ===============================
        if (userRepository.findByEmail("admin@celebrationpoint.com").isEmpty()) {

            User admin = new User();
            admin.setFullName("Super Admin");
            admin.setEmail("admin@celebrationpoint.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
        }
    }
}
