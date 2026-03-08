package studio.humpback.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TwoFactorStatusResponse {
    private Boolean twoFactorEnabled;
}
