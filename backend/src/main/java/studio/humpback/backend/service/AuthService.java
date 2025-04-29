package studio.humpback.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import studio.humpback.backend.model.User;
import studio.humpback.backend.model.UserRole;
import studio.humpback.backend.repository.UserRepository;
import studio.humpback.backend.exception.AuthorizationException;
import studio.humpback.backend.exception.ResourceNotFoundException;

import java.util.Optional;
import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_PASSWORD = "Invalid password";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${sysadmin.username}")
    private String sysadminUsername;

    @Value("${sysadmin.fullname}")
    private String sysadminFullname;

    @Value("${sysadmin.email}")
    private String sysadminEmail;

    @Value("${sysadmin.password}")
    private String sysadminPassword;

    @PostConstruct
    public void createSysadminIfNeeded() {
        Boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRoles().contains(UserRole.ADMIN));

        if (adminExists) {
            logger.info("[SYSADMIN] Admin user already exists, skipping creation.");
            return;
        }

        User sysadmin = User.builder()
                .username(sysadminUsername)
                .fullName(sysadminFullname)
                .email(sysadminEmail)
                .password(passwordEncoder.encode(sysadminPassword))
                .roles(Collections.singletonList(UserRole.ADMIN))
                .createdAt(Instant.now())
                .passwordExpiredAt(Instant.now())
                .build();

        userRepository.save(sysadmin);

        logger.info("[SYSADMIN] Initial admin user created successfully.");
    }

    public User authenticate(String username, String rawPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException(INVALID_PASSWORD);
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return user;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }
}
