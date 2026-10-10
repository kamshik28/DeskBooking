package com.kaminskyi.deskbooking.service;

import com.kaminskyi.deskbooking.dto.AuthResponse;
import com.kaminskyi.deskbooking.dto.LoginRequest;
import com.kaminskyi.deskbooking.dto.RegisterRequest;
import com.kaminskyi.deskbooking.dto.UserResponse;
import com.kaminskyi.deskbooking.entity.Role;
import com.kaminskyi.deskbooking.entity.User;
import com.kaminskyi.deskbooking.exception.ConflictException;
import com.kaminskyi.deskbooking.exception.NotFoundException;
import com.kaminskyi.deskbooking.repository.UserRepository;
import com.kaminskyi.deskbooking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> NotFoundException.of("User", id));
    }
}
