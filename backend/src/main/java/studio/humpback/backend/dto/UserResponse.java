package studio.humpback.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import studio.humpback.backend.model.UserRole;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String fullName;
    private List<UserRole> roles;
    private String email;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant lastLogin;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant passwordExpiredAt;

    private Boolean accountLocked;

    private String token;
}
