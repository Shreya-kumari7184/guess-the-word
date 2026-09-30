package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.AuthResponse;
import com.guessword.guesstheword.dto.LoginRequest;
import com.guessword.guesstheword.dto.RegisterRequest;
import com.guessword.guesstheword.entity.Role;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.exception.DuplicateUsernameException;
import com.guessword.guesstheword.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateUsernameException(request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PLAYER);
        User saved = userRepository.save(user);
        return new AuthResponse(saved.getUsername(), saved.getRole(), false, "Registration successful");
    }

    public Authentication authenticate(LoginRequest request) {
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getUsername(), request.getPassword()));
    }
}
