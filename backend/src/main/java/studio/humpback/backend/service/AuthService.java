package studio.humpback.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
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
import studio.humpback.backend.exception.PasswordChangeRequiredException;
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
    private static final String PASSWORD_CHANGE_REQUIRED = "Password change required";
    private static final String USER_ACCOUNT_LOCKED = "User Account Locked";
    private static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    private static final String USERNAME_REQUIRED = "Username is required";
    private static final String USERNAME_POLICY_INVALID = "Username policy invalid";
    private static final String USERNAME_RESERVED = "Username is reserved";
    private static final String NEW_PASSWORD_REQUIRED = "New password is required";
    private static final String NEW_PASSWORD_LENGTH_INVALID = "New password must be between 12 and 64 characters";
    private static final String NEW_PASSWORD_POLICY_INVALID =
            "Password must contain uppercase, lowercase, number, and special character, with no spaces";
    private static final String NEW_PASSWORD_MUST_DIFFERENT = "New password must be different from current password";
    private static final String NEW_PASSWORD_PREVIOUSLY_USED = "New password was previously used";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9._-]{2,31}$");
    private static final Pattern PASSWORD_UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_SPECIAL_PATTERN = Pattern.compile(".*[^A-Za-z0-9].*");
    private static final int PASSWORD_MIN_LENGTH = 12;
    private static final int PASSWORD_MAX_LENGTH = 64;
    private static final Set<String> RESERVED_USERNAMES = Set.of(
            "admin", "administrator", "root", "system", "support", "api", "security");

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

    @Value("${security.user-accounts.password-change-threshold-days:5}")
    private Integer passwordChangeThresholdDays;

    @PostConstruct
    public void createSysadminIfNeeded() {
        Boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRoles().contains(UserRole.ADMIN));

        if (adminExists) {
            logger.info("[SYSADMIN] Admin user already exists, skipping creation.");
            return;
        }

        User sysadmin = User.builder()
                .username(normalizeUsername(sysadminUsername))
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
                .twoFactorEnabled(Boolean.FALSE)
                .build();

        userRepository.save(sysadmin);

        logger.info("[SYSADMIN] Initial admin user created successfully.");
    }

    public User authenticate(String username, String rawPassword) {
        String normalizedUsername = normalizeUsername(username);
        Optional<User> userOptional = userRepository.findByUsernameIgnoreCase(normalizedUsername);

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

        if (isPasswordChangeRequired(user)) {
            logger.info("[AUTH] Password change required for user '{}'. Expiration near threshold.", user.getUsername());
            throw new PasswordChangeRequiredException(PASSWORD_CHANGE_REQUIRED);
        }

        return user;
    }

    public User completeSuccessfulLogin(User user) {
        user.setLastLogin(Instant.now());
        user.setNumberOfFailedAttempts(0);
        return userRepository.save(user);
    }

    public User getUserByUsername(String username) {
        String normalizedUsername = normalizeUsername(username);
        return userRepository.findByUsernameIgnoreCase(normalizedUsername)
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
        String normalizedUsername = normalizeAndValidateUsername(registerRequest.getUsername(), true);
        Optional<User> existingUser = userRepository.findByUsernameIgnoreCase(normalizedUsername);

        if (existingUser.isPresent()) {
            throw new IllegalArgumentException(USERNAME_ALREADY_EXISTS);
        }

        User newUser = User.builder()
                .username(normalizedUsername)
                .fullname(registerRequest.getFullname())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(validateAndNormalizeNewPassword(registerRequest.getPassword())))
                .roles(Collections
                        .singletonList(UserRole.READER)
                        .stream()
                        .collect(Collectors.toSet()))
                .createdAt(Instant.now())
                .passwordExpiredAt(Instant.now().plus(passwordExpirationDays, ChronoUnit.DAYS))
                .disabled(Boolean.TRUE)
                .twoFactorEnabled(Boolean.FALSE)
                .build();

        userRepository.save(newUser);
    }

    public void resetPassword(User user, String newPassword) {
        applyNewPassword(user, newPassword);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);

        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new UserAccountLockedException(USER_DISABLED);
        }

        if (Boolean.TRUE.equals(user.getAccountLocked()) && !Boolean.TRUE.equals(user.isPasswordExpired())) {
            throw new UserAccountLockedException(USER_ACCOUNT_LOCKED);
        }

        validatePassword(oldPassword, user);
        applyNewPassword(user, newPassword);
    }

    private String normalizeUsername(String username) {
        return Optional.ofNullable(username)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private String normalizeAndValidateUsername(String username, boolean checkReserved) {
        String normalized = Optional.ofNullable(username)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException(USERNAME_REQUIRED));

        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(USERNAME_POLICY_INVALID);
        }

        if (checkReserved && RESERVED_USERNAMES.contains(normalized)) {
            throw new IllegalArgumentException(USERNAME_RESERVED);
        }

        return normalized;
    }

    private String validateAndNormalizeNewPassword(String password) {
        String normalizedPassword = Optional.ofNullable(password)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new IllegalArgumentException(NEW_PASSWORD_REQUIRED));

        if (normalizedPassword.length() < PASSWORD_MIN_LENGTH || normalizedPassword.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException(NEW_PASSWORD_LENGTH_INVALID);
        }

        boolean hasUppercase = PASSWORD_UPPERCASE_PATTERN.matcher(normalizedPassword).matches();
        boolean hasLowercase = PASSWORD_LOWERCASE_PATTERN.matcher(normalizedPassword).matches();
        boolean hasDigit = PASSWORD_DIGIT_PATTERN.matcher(normalizedPassword).matches();
        boolean hasSpecial = PASSWORD_SPECIAL_PATTERN.matcher(normalizedPassword).matches();
        boolean hasWhitespace = normalizedPassword.chars().anyMatch(Character::isWhitespace);

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial || hasWhitespace) {
            throw new IllegalArgumentException(NEW_PASSWORD_POLICY_INVALID);
        }

        return normalizedPassword;
    }

    private boolean isPasswordChangeRequired(User user) {
        Instant thresholdDate = Instant.now().plus(passwordChangeThresholdDays, ChronoUnit.DAYS);
        return Optional.ofNullable(user.getPasswordExpiredAt())
                .map(expiration -> !expiration.isAfter(thresholdDate))
                .orElse(true);
    }

    private void applyNewPassword(User user, String newPassword) {
        String normalizedPassword = validateAndNormalizeNewPassword(newPassword);

        if (passwordEncoder.matches(normalizedPassword, user.getPassword())) {
            throw new IllegalArgumentException(NEW_PASSWORD_MUST_DIFFERENT);
        }

        boolean passwordUsedBefore = Optional.ofNullable(user.getOlderPasswords())
                .orElseGet(Set::of)
                .stream()
                .anyMatch(oldPasswordHash -> passwordEncoder.matches(normalizedPassword, oldPasswordHash));
        if (passwordUsedBefore) {
            throw new IllegalArgumentException(NEW_PASSWORD_PREVIOUSLY_USED);
        }

        Set<String> olderPasswords = Optional.ofNullable(user.getOlderPasswords())
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        olderPasswords.add(user.getPassword());
        user.setOlderPasswords(olderPasswords);
        user.setPassword(passwordEncoder.encode(normalizedPassword));
        user.setPasswordExpiredAt(Instant.now().plus(passwordExpirationDays, ChronoUnit.DAYS));
        user.setAccountLocked(Boolean.FALSE);
        user.setNumberOfFailedAttempts(0);
        userRepository.save(user);
    }

}
