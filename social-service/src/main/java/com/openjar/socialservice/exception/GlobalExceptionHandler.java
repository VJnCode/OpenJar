package com.openjar.socialservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
        return body(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ResponseEntity<Map<String, Object>> response = body(HttpStatus.BAD_REQUEST,
                fieldErrors.values().stream().findFirst().orElse("Validation failed"));
        response.getBody().put("fieldErrors", fieldErrors);
        return response;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            NumberFormatException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return body(HttpStatus.BAD_REQUEST, "Malformed request");
    }

    /** Never leak internal exception messages to clients. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled error", ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
