package studio.humpback.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.LoginRequest;
import studio.humpback.backend.dto.RegisterRequest;
import studio.humpback.backend.dto.UserResponse;
import studio.humpback.backend.model.User;
import studio.humpback.backend.service.AuthService;
import studio.humpback.backend.security.JwtTokenProvider;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<UserResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        
        String token = jwtTokenProvider.createToken(
            user.getUsername(),
            user.getRoles()
        );

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .roles(user.getRoles())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLogin())
                .token(token)
                .build();

        return ApiResponse.success(userResponse);
    }

    @GetMapping("/me")
    @ResponseStatus(code = HttpStatus.OK)
    public ApiResponse<UserResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        User user = authService.getUserByUsername(username);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .roles(user.getRoles())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLogin())
                .build();

        return ApiResponse.success(userResponse);
    }

    @PostMapping("/register")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ApiResponse<?> register(@RequestBody @Valid RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ApiResponse.success();
    }
}
