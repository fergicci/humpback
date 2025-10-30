package studio.humpback.backend.exception;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import studio.humpback.backend.dto.ApiError;
import studio.humpback.backend.dto.ApiResponse;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String NO_DETAIL = "";

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex,
            Locale locale) {
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), NO_DETAIL);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), NO_DETAIL);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthorizationException(AuthorizationException ex) {
        log.error("Authorization error: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), NO_DETAIL);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), NO_DETAIL);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Internal server error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), NO_DETAIL);
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
