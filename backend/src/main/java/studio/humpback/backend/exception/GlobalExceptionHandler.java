package studio.humpback.backend.exception;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;

import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import studio.humpback.backend.dto.ApiResponse;
import studio.humpback.backend.dto.ApiError;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor

public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";
    
    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException ex,
        Locale locale
    ) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + messageSource.getMessage(fieldError, locale))
                .toList();

        ApiError apiError = ApiError.builder()
                .timestamp(new Date())
                .code(HttpStatus.BAD_REQUEST.value())
                .message(VALIDATION_FAILED)
                .details(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(apiError));
    }

    @ExceptionHandler({
        AuthenticationException.class,
        PasswordExpiredException.class,
        UserAccountLockedException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(RuntimeException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed", ex.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationException(AuthorizationException ex) {
        log.error("Authorization error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Authorization failed", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Internal server error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getMessage());
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(HttpStatus status, String message, String detail) {
        ApiError error = ApiError.builder()
                .timestamp(new Date())
                .code(status.value())
                .message(message)
                .details(Collections.singletonList(detail))
                .build();

        return ResponseEntity.status(status).body(ApiResponse.error(error));
    }
}
