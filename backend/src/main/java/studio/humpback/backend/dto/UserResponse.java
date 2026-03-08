package studio.humpback.backend.dto;

import java.time.Instant;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import studio.humpback.backend.model.UserRole;

@Getter
@Setter
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String fullname;
    private Set<UserRole> roles;
    private String email;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant lastLoginAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant passwordExpiredAt;

    private Boolean accountLocked;
    private Boolean disabled;
    private Boolean twoFactorEnabled;
    private Boolean requiresTwoFactor;
    private String twoFactorChallengeToken;
    private String token;
}
