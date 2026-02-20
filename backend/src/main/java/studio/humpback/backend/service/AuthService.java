package studio.humpback.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.RegisterRequest;
import studio.humpback.backend.exception.PasswordExpiredException;
import studio.humpback.backend.exception.ResourceNotFoundException;
import studio.humpback.backend.exception.UserAccountLockedException;
import studio.humpback.backend.model.User;
import studio.humpback.backend.model.UserRole;
import studio.humpback.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final String USER_NOT_FOUND = "User not found";
    private static final String USER_DISABLED = "User disabled";
    private static final String INVALID_PASSWORD = "Invalid password";
    private static final String EXPIRED_PASSWORD = "Expired password";
    private static final String USER_ACCOUNT_LOCKED = "User Account Locked";
    private static final String USERNAME_ALREADY_EXISTS = "Username already exists";

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

    @Value("${security.user-accounts.max-number-of-attempts:3}")
    private Integer maxNumberOfAttempts;

    @Value("${security.user-accounts.password-expiration-days:90}")
    private Integer passwordExpirationDays;

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
                .fullname(sysadminFullname)
                .email(sysadminEmail)
                .password(passwordEncoder.encode(sysadminPassword))
                .roles(Collections
                        .singletonList(UserRole.ADMIN)
                        .stream()
                        .collect(Collectors.toSet()))
                .createdAt(Instant.now())
                .passwordExpiredAt(Instant.now()
                        .plus(passwordExpirationDays, ChronoUnit.DAYS))
                .disabled(Boolean.FALSE)
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

        if (user.getDisabled()) {
            logger.warn("[AUTH] Account is disabled for user '{}'.", user.getUsername());
            throw new UserAccountLockedException(USER_DISABLED);
        }

        if (user.isAccountLocked()) {
            logger.warn("[AUTH] Account is locked for user '{}'.", user.getUsername());
            throw new UserAccountLockedException(USER_ACCOUNT_LOCKED);
        }

        if (user.isPasswordExpired()) {
            logger.warn("[AUTH] Password is expired for user '{}'.", user.getUsername());
            throw new PasswordExpiredException(EXPIRED_PASSWORD);
        }

        validatePassword(rawPassword, user);

        user.setLastLogin(Instant.now());
        userRepository.save(user);

        return user;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private void validatePassword(String rawPassword, User user) {
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            return;
        }

        user.setNumberOfFailedAttempts(
                Optional.ofNullable(user.getNumberOfFailedAttempts()).orElse(0) + 1);

        if (user.getNumberOfFailedAttempts() > maxNumberOfAttempts) {
            user.setAccountLocked(true);
        }

        userRepository.save(user);

        logger.warn("[AUTH] Failed login attempt for user '{}'. Attempts: {}", user.getUsername(),
                user.getNumberOfFailedAttempts());
        throw new BadCredentialsException(INVALID_PASSWORD);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .accountLocked(user.isAccountLocked())
                .disabled(user.getDisabled())
                .build();
    }

    public void registerUser(RegisterRequest registerRequest) {
        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());

        if (existingUser.isPresent()) {
            throw new IllegalArgumentException(USERNAME_ALREADY_EXISTS);
        }

        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .fullname(registerRequest.getFullname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Collections
                        .singletonList(UserRole.READER)
                        .stream()
                        .collect(Collectors.toSet()))
                .createdAt(Instant.now())
                .passwordExpiredAt(Instant.now().plus(passwordExpirationDays, ChronoUnit.DAYS))
                .disabled(Boolean.TRUE)
                .build();

        userRepository.save(newUser);
    }

}
