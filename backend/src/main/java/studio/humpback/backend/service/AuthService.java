package studio.humpback.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import studio.humpback.backend.model.User;
import studio.humpback.backend.repository.UserRepository;
import studio.humpback.backend.exception.AuthorizationException;
import studio.humpback.backend.exception.ResourceNotFoundException;

import java.util.Optional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_PASSWORD = "Invalid password";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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
