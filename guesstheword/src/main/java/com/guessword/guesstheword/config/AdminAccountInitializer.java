package com.guessword.guesstheword.config;

import com.guessword.guesstheword.entity.Role;
import com.guessword.guesstheword.entity.User;
import com.guessword.guesstheword.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final Environment environment;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(Environment environment, UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String username = environment.getProperty("ADMIN_USERNAME");
        String password = environment.getProperty("ADMIN_PASSWORD");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.info("Admin account initialization skipped: ADMIN_USERNAME or ADMIN_PASSWORD is not configured");
            return;
        }
        if (userRepository.findByUsername(username).isPresent()) {
            log.info("Admin account initialization skipped: the configured username already exists");
            return;
        }

        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Configured admin account initialized");
    }
}
