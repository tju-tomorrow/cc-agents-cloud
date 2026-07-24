package io.ccagents.cloud.shared.error;

import io.ccagents.cloud.auth.EmailAlreadyRegisteredException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError validationFailed(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return error(
                "VALIDATION_FAILED",
                "Request validation failed",
                request,
                details);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiError emailAlreadyRegistered(
            EmailAlreadyRegisteredException exception,
            HttpServletRequest request) {
        return error(
                "EMAIL_ALREADY_REGISTERED",
                exception.getMessage(),
                request,
                Map.of());
    }

    private ApiError error(
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> details) {
        Object existingRequestId = request.getAttribute("requestId");
        String requestId = existingRequestId instanceof String value
                ? value
                : UUID.randomUUID().toString();
        return new ApiError(code, message, requestId, details);
    }
}

