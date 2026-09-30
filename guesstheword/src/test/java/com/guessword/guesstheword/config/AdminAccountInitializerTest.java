package com.guessword.guesstheword.config;

import com.guessword.guesstheword.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.*;

class AdminAccountInitializerTest {

    @Test
    void skipsInitializationWhenAdminEnvironmentVariablesAreAbsent() throws Exception {
        Environment environment = mock(Environment.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(environment.getProperty("ADMIN_USERNAME")).thenReturn(null);
        when(environment.getProperty("ADMIN_PASSWORD")).thenReturn(null);

        AdminAccountInitializer initializer = new AdminAccountInitializer(environment, userRepository, passwordEncoder);
        initializer.run(new DefaultApplicationArguments(new String[0]));

        verifyNoInteractions(userRepository, passwordEncoder);
    }
}
