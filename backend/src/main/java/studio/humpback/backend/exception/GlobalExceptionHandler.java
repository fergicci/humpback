package studio.humpback.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import studio.humpback.backend.dto.ApiError;
import studio.humpback.backend.dto.ApiResponse;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("Validation error", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .details(errors)
                .build();

        return ResponseEntity.badRequest().body(ApiResponse.error(apiError));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllOtherExceptions(Exception ex) {
        logger.error("Unexpected error", ex.getMessage());

        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .details(List.of(ex.getMessage()))
                .build();

        return ResponseEntity.internalServerError().body(ApiResponse.error(apiError));
    }
}