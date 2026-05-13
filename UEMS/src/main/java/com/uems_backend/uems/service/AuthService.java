package com.uems_backend.uems.service;

import com.uems_backend.uems.dto.AuthResponse;
import com.uems_backend.uems.dto.LoginRequest;
import com.uems_backend.uems.dto.RegisterRequest;
import com.uems_backend.uems.exception.BadRequestException;
import com.uems_backend.uems.model.AppUser;
import com.uems_backend.uems.model.Role;
import com.uems_backend.uems.repository.UserRepository;
import com.uems_backend.uems.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new BadRequestException("Password must contain at least 6 characters");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        if (request.role() != null && request.role() != Role.ORGANIZER) {
            throw new BadRequestException("Public registration is only available for organizers");
        }
        Role role = Role.ORGANIZER;
        AppUser user = userRepository.save(new AppUser(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                role
        ));
        return new AuthResponse(jwtService.generateToken(user), user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("Account is disabled");
        }
        return new AuthResponse(jwtService.generateToken(user), user.getUsername(), user.getRole());
    }
}
