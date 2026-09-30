package com.guessword.guesstheword.service;

import com.guessword.guesstheword.dto.LoginRequest;
import com.guessword.guesstheword.dto.RegisterRequest;
import com.guessword.guesstheword.entity.Role;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.exception.DuplicateUsernameException;
import com.guessword.guesstheword.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final AuthService authService = new AuthService(userRepository, passwordEncoder, authenticationManager);
    private static final jakarta.validation.ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validatorFactory.getValidator();

    @BeforeEach
    void resetMocks() {
        reset(userRepository, authenticationManager);
    }

    @org.junit.jupiter.api.AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void validRegistrationCreatesPlayerWithHashedPassword() {
        when(userRepository.findByUsername("Alice")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = authService.register(new RegisterRequest("Alice", "hello1$"));

        var captor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(Role.PLAYER, saved.getRole());
        assertNotEquals("hello1$", saved.getPassword());
        assertTrue(passwordEncoder.matches("hello1$", saved.getPassword()));
        assertEquals(Role.PLAYER, response.role());
        assertFalse(java.util.Arrays.stream(response.getClass().getRecordComponents())
                .anyMatch(component -> component.getName().equalsIgnoreCase("password")));
    }

    @Test
    void duplicateUsernameIsRejected() {
        User existing = new User();
        existing.setUsername("Alice");
        when(userRepository.findByUsername("Alice")).thenReturn(Optional.of(existing));

        assertThrows(DuplicateUsernameException.class,
                () -> authService.register(new RegisterRequest("Alice", "hello1$")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void passwordWithoutRequiredPartsFailsValidation() {
        RegisterRequest request = new RegisterRequest("Alice", "abcdef");
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validLoginAuthenticatesCredentials() {
        Authentication authenticated = UsernamePasswordAuthenticationToken.authenticated(
                "Alice", null, java.util.List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authenticated);

        assertSame(authenticated, authService.authenticate(new LoginRequest("Alice", "hello1$")));
    }

    @Test
    void invalidLoginCredentialsAreRejected() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(new LoginRequest("Alice", "wrong")));
    }

    @Test
    void adminAndPlayerRolesAreExposedAsSecurityAuthorities() {
        CustomUserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("encoded");
        admin.setRole(Role.ADMIN);
        User player = new User();
        player.setUsername("player");
        player.setPassword("encoded");
        player.setRole(Role.PLAYER);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("player")).thenReturn(Optional.of(player));

        assertEquals("ROLE_ADMIN", userDetailsService.loadUserByUsername("admin")
                .getAuthorities().iterator().next().getAuthority());
        assertEquals("ROLE_PLAYER", userDetailsService.loadUserByUsername("player")
                .getAuthorities().iterator().next().getAuthority());
    }
}
