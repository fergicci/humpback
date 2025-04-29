package studio.humpback.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    private List<UserRole> roles;
    private String fullName;
    private String email;
    private Instant createdAt;
    private Instant lastLogin;
    private Instant passwordExpiredAt;

    public Boolean isPasswordExpired() {
        return Optional.ofNullable(this.passwordExpiredAt)
            .map(expiration -> expiration.isBefore(Instant.now()))
            .orElse(true);
    }
}
