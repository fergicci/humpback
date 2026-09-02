package studio.humpback.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import studio.humpback.backend.model.User;
import studio.humpback.backend.model.UserRole;
import studio.humpback.backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    public static final String AUTHORITY_ADMIN = "ADMIN";
    public static final String AUTHORITY_READER = "READER";
    public static final String AUTHORITY_ROLE_ADMIN = "ROLE_ADMIN";
    public static final String AUTHORITY_ROLE_READER = "ROLE_READER";

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_NOT_FOUND = "User with id %s not found";
    private static final String USER_DELETED = "User with id %s has been deleted";
    private static final String USED_DISABLED = "User with id %s has been %s";
    private static final String USER_LOCKED = "User with id %s has been %s";
    private static final String UNLOCKED = "unlocked";
    private static final String LOCKED = "locked";
    private static final String DISABLED = "disabled";
    private static final String ENABLED = "enabled";
    private static final String ROLE_MUST_BE_SINGLE = "User must have exactly one role";

    @Value("${security.user-accounts.password-expiration-days:90}")
    private Integer passwordExpirationDays;

    private final UserRepository userRepository;

    public Page<User> getPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User update(String id, String fullname, String email,
            boolean disabled, boolean accountLocked, Set<String> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(USER_NOT_FOUND, id)));

        Set<String> normalizedRoles = Optional.ofNullable(roles)
                .filter(value -> value.size() == 1)
                .orElseThrow(() -> new IllegalArgumentException(ROLE_MUST_BE_SINGLE));

        user.setFullname(fullname);
        user.setEmail(email);
        user.setDisabled(disabled);
        user.setAccountLocked(accountLocked);
        user.setRoles(normalizedRoles.stream().map(UserRole::valueOf).collect(Collectors.toSet()));

        return userRepository.save(user);
    }

    public void delete(String id) {
        userRepository.deleteById(id);

        logger.info(String.format(USER_DELETED, id));
    }

    public void disable(String id,  Boolean desiredDisable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(USER_NOT_FOUND, id)));

        Boolean normalizedDesiredDisable = Optional.ofNullable(desiredDisable).orElse(Boolean.FALSE);
        user.setDisabled(normalizedDesiredDisable);
        userRepository.save(user);

        logger.info(String.format(USED_DISABLED, id, normalizedDesiredDisable ? DISABLED : ENABLED));
    }

    public void lock(String id, Boolean desiredLock) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(USER_NOT_FOUND, id)));

        Boolean normalizedDesiredLock = Optional.ofNullable(desiredLock).orElse(Boolean.FALSE);
        user.setAccountLocked(normalizedDesiredLock);

        Instant now = Instant.now();
        boolean isPasswordExpired = Optional.ofNullable(user.getPasswordExpiredAt())
                .map(expiration -> expiration.isBefore(now))
                .orElse(Boolean.TRUE);
        if (isPasswordExpired) {
            int expirationDays = Optional.ofNullable(passwordExpirationDays).orElse(90);
            user.setPasswordExpiredAt(now.plus(expirationDays, ChronoUnit.DAYS));
        }

        userRepository.save(user);

        logger.info(String.format(USER_LOCKED, id, normalizedDesiredLock ? LOCKED : UNLOCKED));
    }
}
