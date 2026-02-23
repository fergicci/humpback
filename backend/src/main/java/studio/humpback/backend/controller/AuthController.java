package studio.humpback.backend.controller;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.ForgotPasswordChallengeResponse;
import studio.humpback.backend.dto.ForgotPasswordRequest;
import studio.humpback.backend.dto.ForgotPasswordResetRequest;
import studio.humpback.backend.dto.LoginRequest;
import studio.humpback.backend.dto.NewPasswordRequest;
import studio.humpback.backend.dto.RegisterRequest;
import studio.humpback.backend.dto.TwoFactorCodeRequest;
import studio.humpback.backend.dto.TwoFactorLoginRequest;
import studio.humpback.backend.dto.TwoFactorSetupResponse;
import studio.humpback.backend.dto.TwoFactorStatusResponse;
import studio.humpback.backend.dto.UserResponse;
import studio.humpback.backend.model.User;
import studio.humpback.backend.service.AuthService;
import studio.humpback.backend.service.TwoFactorService;
import studio.humpback.backend.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String API_LOGIN = "/login";
    private static final String API_LOGIN_2FA = "/login/2fa";
    private static final String API_2FA_SETUP = "/2fa/setup";
    private static final String API_2FA_ENABLE = "/2fa/enable";
    private static final String API_2FA_DISABLE = "/2fa/disable";
    private static final String API_FORGOT_PASSWORD_REQUEST = "/forgot-password/request";
    private static final String API_FORGOT_PASSWORD_RESET = "/forgot-password/reset";
    private static final String API_CHANGE_PASSWORD = "/change-password";
    private static final String API_ME = "/me";
    private static final String API_REGISTER = "/register";
    private static final String ERR_INVALID_2FA_CODE = "Invalid 2FA code";
    private static final String ERR_USER_DISABLED = "User disabled";
    private static final String ERR_FORGOT_PASSWORD_REQUIRES_2FA = "Forgot password requires 2FA to be enabled";

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TwoFactorService twoFactorService;

    @PostMapping(API_LOGIN)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<UserResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled()) && hasTwoFactorSecret(user)) {
            String challengeToken = jwtTokenProvider.createTwoFactorChallengeToken(user.getUsername());
            UserResponse userResponse = toResponse(user, null, true, challengeToken);
            return ApiResponse.success(userResponse);
        }

        user = authService.completeSuccessfulLogin(user);
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        UserResponse userResponse = toResponse(user, token, false, null);
        return ApiResponse.success(userResponse);
    }

    @PostMapping(API_LOGIN_2FA)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<UserResponse> loginWithTwoFactor(@RequestBody @Valid TwoFactorLoginRequest request) {
        String username = jwtTokenProvider.getUsernameFromTwoFactorChallengeToken(request.getChallengeToken());
        User user = authService.getUserByUsername(username);

        if (!twoFactorService.verify(user, request.getCode())) {
            throw new IllegalArgumentException(ERR_INVALID_2FA_CODE);
        }

        user = authService.completeSuccessfulLogin(user);
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRoles());
        return ApiResponse.success(toResponse(user, token, false, null));
    }

    @PostMapping(API_2FA_SETUP)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<TwoFactorSetupResponse> setupTwoFactor() {
        User user = getCurrentAuthenticatedUser();
        return ApiResponse.success(twoFactorService.startSetup(user));
    }

    @PostMapping(API_2FA_ENABLE)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<TwoFactorStatusResponse> enableTwoFactor(@RequestBody @Valid TwoFactorCodeRequest request) {
        User user = getCurrentAuthenticatedUser();
        boolean enabled = twoFactorService.enable(user, request.getCode());
        return ApiResponse.success(TwoFactorStatusResponse.builder().twoFactorEnabled(enabled).build());
    }

    @PostMapping(API_2FA_DISABLE)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<TwoFactorStatusResponse> disableTwoFactor(@RequestBody @Valid TwoFactorCodeRequest request) {
        User user = getCurrentAuthenticatedUser();
        boolean enabled = twoFactorService.disable(user, request.getCode());
        return ApiResponse.success(TwoFactorStatusResponse.builder().twoFactorEnabled(enabled).build());
    }

    @PostMapping(API_FORGOT_PASSWORD_REQUEST)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<ForgotPasswordChallengeResponse> requestForgotPassword(
            @RequestBody @Valid ForgotPasswordRequest request) {
        User user = authService.getUserByUsername(request.getUsername());

        if (Boolean.TRUE.equals(user.getDisabled())) {
            throw new IllegalArgumentException(ERR_USER_DISABLED);
        }

        if (!Boolean.TRUE.equals(user.getTwoFactorEnabled()) || !hasTwoFactorSecret(user)) {
            throw new IllegalArgumentException(ERR_FORGOT_PASSWORD_REQUIRES_2FA);
        }

        String challengeToken = jwtTokenProvider.createForgotPasswordChallengeToken(user.getUsername());
        return ApiResponse.success(
                ForgotPasswordChallengeResponse.builder()
                        .requiresTwoFactor(Boolean.TRUE)
                        .challengeToken(challengeToken)
                        .build());
    }

    @PostMapping(API_FORGOT_PASSWORD_RESET)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<Object> resetForgotPassword(@RequestBody @Valid ForgotPasswordResetRequest request) {
        String username = jwtTokenProvider.getUsernameFromForgotPasswordChallengeToken(request.getChallengeToken());
        User user = authService.getUserByUsername(username);

        if (!twoFactorService.verify(user, request.getCode())) {
            throw new IllegalArgumentException(ERR_INVALID_2FA_CODE);
        }

        authService.resetPassword(user, request.getNewPassword());
        return ApiResponse.success();
    }

    @PostMapping(API_CHANGE_PASSWORD)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<Object> changePassword(@RequestBody @Valid NewPasswordRequest request) {
        authService.changePassword(
                request.getUsername(),
                request.getOldPassword(),
                request.getNewPassword());
        return ApiResponse.success();
    }

    private User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return authService.getUserByUsername(username);
    }

    private boolean hasTwoFactorSecret(User user) {
        return Optional.ofNullable(user.getTwoFactorSecret())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .isPresent();
    }

    private UserResponse toResponse(User user, String token, boolean requiresTwoFactor, String challengeToken) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .roles(user.getRoles())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLogin())
                .twoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()))
                .requiresTwoFactor(requiresTwoFactor)
                .twoFactorChallengeToken(challengeToken)
                .token(token)
                .build();
    }

    @GetMapping(API_ME)
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<UserResponse> me() {
        User user = getCurrentAuthenticatedUser();
        return ApiResponse.success(toResponse(user, null, false, null));
    }

    @PostMapping(API_REGISTER)
    @ResponseStatus(code = HttpStatus.CREATED)
    public ApiResponse<?> register(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ApiResponse.success();
    }
}
