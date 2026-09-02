package studio.humpback.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFactorLoginRequest {
    @NotBlank
    private String challengeToken;

    @NotBlank
    private String code;
}
