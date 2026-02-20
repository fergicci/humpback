package studio.humpback.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPasswordRequest {
    private String username;
    private String oldPassword;
    private String newPassword;
}
