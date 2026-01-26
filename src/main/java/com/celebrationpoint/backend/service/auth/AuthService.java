package com.celebrationpoint.backend.service.auth;

import com.celebrationpoint.backend.constants.RoleType;
import com.celebrationpoint.backend.entity.Role;
import com.celebrationpoint.backend.entity.User;
import com.celebrationpoint.backend.repository.RoleRepository;
import com.celebrationpoint.backend.repository.UserRepository;
import com.celebrationpoint.backend.config.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // ✅ MANUAL CONSTRUCTOR (BEST PRACTICE)
    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // ✅ REGISTER
    public String register(String fullName, String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        Role role = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() ->
                        roleRepository.save(new Role(RoleType.ROLE_USER))
                );

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.addRole(role);

        userRepository.save(user);

        return "User registered successfully";
    }

    // ✅ LOGIN
    public String login(String email, String password) {

        // 🔐 authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // 🔍 load user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // 🔐 generate JWT (ROLE inside token)
        return jwtService.generateToken(user);
    }
}
