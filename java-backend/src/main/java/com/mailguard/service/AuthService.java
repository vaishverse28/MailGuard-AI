package com.mailguard.service;

import com.mailguard.dto.AuthResponse;
import com.mailguard.dto.LoginRequest;
import com.mailguard.dto.RegisterRequest;
import com.mailguard.dto.UserDto;
import com.mailguard.exception.ApiException;
import com.mailguard.model.User;
import com.mailguard.repository.UserRepository;
import com.mailguard.security.JwtUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw ApiException.badRequest("Email is already registered");
        }
        User user = new User();
        user.setFullName(req.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // two requests ek-sathi aale tar unique constraint pakdto
            throw ApiException.badRequest("Email is already registered");
        }
        return new AuthResponse(true, "Registration successful", UserDto.from(user), null);
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        return new AuthResponse(true, "Login successful", UserDto.from(user), jwtUtil.generateToken(user));
    }
}
