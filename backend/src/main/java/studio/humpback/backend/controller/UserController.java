package studio.humpback.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.PagedResponse;
import studio.humpback.backend.dto.UserRequest;
import studio.humpback.backend.dto.UserResponse;
import studio.humpback.backend.model.User;
import studio.humpback.backend.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'READER')")
    public ApiResponse<PagedResponse<UserResponse>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.ASC, "username"));
        Page<User> usersPage = userService.getPage(pageable);

        List<UserResponse> content = usersPage.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        PagedResponse<UserResponse> pagedResponse = PagedResponse.<UserResponse>builder()
                .content(content)
                .page(usersPage.getNumber())
                .size(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .build();

        return ApiResponse.success(pagedResponse);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<UserResponse> updateUser(@PathVariable String id, @RequestBody @Valid UserRequest userRequest) {
        User updatedUser = userService.update(
                id,
                userRequest.getFullname(),
                userRequest.getEmail(),
                userRequest.getPasswordExpiredAt().toInstant(),
                userRequest.isDisabled(),
                userRequest.isAccountLocked(),
                userRequest.getRoles());

        return ApiResponse.success(toResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteContact(@PathVariable String id) {
        userService.delete(id);
    }

    @PatchMapping("/{id}/disable/{desiredDisable}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Object> setUserDisable(@PathVariable String id, @PathVariable Boolean desiredDisable) {
        userService.disable(id, desiredDisable);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/lock/{desiredLock}")
    @ResponseStatus(code = HttpStatus.OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Object> setUserLocked(@PathVariable String id, @PathVariable Boolean desiredLock) {
        userService.lock(id, desiredLock);
        return ApiResponse.success();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullname(user.getFullname())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLogin())
                .passwordExpiredAt(user.getPasswordExpiredAt())
                .roles(user.getRoles())
                .accountLocked(user.isAccountLocked())
                .disabled(user.getDisabled())
                .build();
    }
}
