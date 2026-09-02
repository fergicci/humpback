package studio.humpback.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForgotPasswordChallengeResponse {
    private Boolean requiresTwoFactor;
    private String challengeToken;
}
