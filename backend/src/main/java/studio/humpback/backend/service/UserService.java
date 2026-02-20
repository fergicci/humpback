package studio.humpback.backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

    @Value("${security.user-accounts.password-expiration-days:90}")
    private Integer passwordExpirationDays;

    private final UserRepository userRepository;

    public Page<User> getPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public User update(String id, String fullname, String email, Instant passwordExpiredAt,
            boolean disabled, boolean accountLocked, Set<String> roles) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(USER_NOT_FOUND, id)));

        user.setFullname(fullname);
        user.setEmail(email);
        user.setPasswordExpiredAt(passwordExpiredAt);
        user.setDisabled(disabled);
        user.setAccountLocked(accountLocked);
        user.setRoles(roles.stream().map(UserRole::valueOf).collect(Collectors.toSet()));

        return userRepository.save(user);
    }

    public void delete(String id) {
        userRepository.deleteById(id);

        logger.info(String.format(USER_DELETED, id));
    }

    public void disable(String id,  Boolean desiredDisable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(USER_NOT_FOUND, id)));

        user.setDisabled(desiredDisable);
        userRepository.save(user);

        logger.info(String.format(USED_DISABLED, id, desiredDisable ? DISABLED : ENABLED));
    }

    public void lock(String id, Boolean desiredLock) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format(USER_NOT_FOUND, id)));

        user.setAccountLocked(desiredLock);
        
        if (user.getPasswordExpiredAt().isBefore(Instant.now()))
            user.setPasswordExpiredAt(Instant.now().plus(passwordExpirationDays, ChronoUnit.DAYS));

        userRepository.save(user);

        logger.info(String.format(USER_LOCKED, id, desiredLock ? LOCKED : UNLOCKED));
    }
}
