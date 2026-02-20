package studio.humpback.backend.model;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;

    @Builder.Default
    private Set<String> olderPasswords = Collections.emptySet();

    private Set<UserRole> roles;
    private String fullname;
    private String email;
    private Instant createdAt;
    private Instant lastLogin;
    private Instant passwordExpiredAt;

    @Builder.Default
    private Integer numberOfFailedAttempts = 0;

    @Builder.Default
    private Boolean accountLocked = false;

    @Builder.Default
    private Boolean disabled = false;

    public Boolean isPasswordExpired() {
        return Optional.ofNullable(this.passwordExpiredAt)
                .map(expiration -> expiration.isBefore(Instant.now()))
                .orElse(true);
    }

    public Boolean isAccountLocked() {
        return isPasswordExpired() || Optional.ofNullable(this.accountLocked).orElse(true);
    }

    public void addOldPassword(String password) {
        this.olderPasswords.add(password);
    }
}
