package studio.humpback.backend.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import studio.humpback.backend.dto.ApiError;
import studio.humpback.backend.dto.ApiResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

        private static final String FORBIDDEN = "Forbidden";
        private static final String JSON_CONTENT_TYPE = "application/json";
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
                ApiError error = ApiError.builder()
                                .timestamp(new Date())
                                .code(HttpServletResponse.SC_FORBIDDEN)
                                .message(FORBIDDEN)
                                .details(Collections.singletonList(accessDeniedException.getMessage()))
                                .build();

                ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                                .success(false)
                                .error(error)
                                .build();

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(JSON_CONTENT_TYPE);
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                objectMapper.writeValue(response.getOutputStream(), apiResponse);
        }
}
