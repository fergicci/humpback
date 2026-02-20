package studio.humpback.backend.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import studio.humpback.backend.config.MessageConfig;
import studio.humpback.backend.model.User;
import studio.humpback.backend.model.UserRole;
import studio.humpback.backend.security.JwtTokenProvider;
import studio.humpback.backend.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MessageConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getUsersReturnsPagedResponse() throws Exception {
        User user = User.builder()
                .id("u1")
                .username("alice")
                .fullname("Alice Doe")
                .email("alice@example.com")
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .lastLogin(Instant.parse("2026-01-02T10:00:00Z"))
                .passwordExpiredAt(Instant.parse("2026-12-31T23:59:00Z"))
                .roles(Set.of(UserRole.ADMIN))
                .accountLocked(false)
                .disabled(false)
                .build();

        when(userService.getPage(any())).thenReturn(new PageImpl<>(java.util.List.of(user), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/users?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("u1"))
                .andExpect(jsonPath("$.data.content[0].username").value("alice"))
                .andExpect(jsonPath("$.data.content[0].lastLoginAt").exists())
                .andExpect(jsonPath("$.data.content[0].passwordExpiredAt").exists());
    }

    @Test
    void updateUserAcceptsIsoDateAndCallsService() throws Exception {
        User updated = User.builder()
                .id("u1")
                .username("alice")
                .fullname("Alice Updated")
                .email("alice.updated@example.com")
                .createdAt(Instant.parse("2026-01-01T10:00:00Z"))
                .lastLogin(Instant.parse("2026-01-02T10:00:00Z"))
                .passwordExpiredAt(Instant.parse("2026-12-31T23:59:00Z"))
                .roles(Set.of(UserRole.ADMIN, UserRole.READER))
                .accountLocked(false)
                .disabled(false)
                .build();

        when(userService.update(
                "u1",
                "Alice Updated",
                "alice.updated@example.com",
                Instant.parse("2026-12-31T23:59:00Z"),
                false,
                false,
                Set.of("ADMIN", "READER"))).thenReturn(updated);

        String payload = """
                {
                  "fullname": "Alice Updated",
                  "email": "alice.updated@example.com",
                  "passwordExpiredAt": "2026-12-31T23:59:00.000Z",
                  "disabled": false,
                  "accountLocked": false,
                  "roles": ["ADMIN", "READER"]
                }
                """;

        mockMvc.perform(put("/api/v1/users/{id}", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("u1"))
                .andExpect(jsonPath("$.data.fullname").value("Alice Updated"));

        verify(userService).update(
                "u1",
                "Alice Updated",
                "alice.updated@example.com",
                Instant.parse("2026-12-31T23:59:00Z"),
                false,
                false,
                Set.of("ADMIN", "READER"));
    }

    @Test
    void updateUserWithInvalidPayloadReturnsBadRequest() throws Exception {
        String payload = objectMapper.writeValueAsString(java.util.Map.of(
                "fullname", "",
                "email", "invalid-email",
                "passwordExpiredAt", "2020-01-01T00:00:00.000Z",
                "disabled", false,
                "accountLocked", false,
                "roles", java.util.List.of()));

        mockMvc.perform(put("/api/v1/users/{id}", "u1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details", hasItem(containsString("fullname"))))
                .andExpect(jsonPath("$.error.details", hasItem(containsString("email"))))
                .andExpect(jsonPath("$.error.details", hasItem(containsString("passwordExpiredAt"))));
    }
}
