package studio.humpback.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordResetRequest {
    @NotBlank
    private String challengeToken;

    @NotBlank
    private String code;

    @NotBlank(message = "{register.request.password.required}")
    @Size(min = 12, max = 64, message = "{register.request.password.size}")
    @Pattern(
            regexp = "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,64}$",
            message = "{register.request.password.pattern}")
    private String newPassword;
}
