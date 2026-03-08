package studio.humpback.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TwoFactorSetupResponse {
    private String manualEntryKey;
    private String otpAuthUrl;
    private String qrCodeDataUri;
    private Boolean twoFactorEnabled;
}
