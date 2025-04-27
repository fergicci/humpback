package studio.humpback.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import studio.humpback.backend.dto.ApiError;
import studio.humpback.backend.dto.ApiResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String UNAUTHORIZED = "Unauthorized";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private final ObjectMapper objectMapper = new ObjectMapper(); // to write JSON manually

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ApiError error = ApiError.builder()
                .timestamp(Instant.now())
                .code(HttpServletResponse.SC_UNAUTHORIZED)
                .message(UNAUTHORIZED)
                .details(Collections.singletonList(authException.getMessage()))
                .build();

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(false)
                .error(error)
                .build();
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(JSON_CONTENT_TYPE);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}