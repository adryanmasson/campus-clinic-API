package com.example.clinic.exceptions;

import com.example.clinic.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiResponse<Object>> handleRuntime(RuntimeException ex) {
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage() == null ? "Internal error" : ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({ EntityNotFoundException.class, NoSuchElementException.class })
    protected ResponseEntity<ApiResponse<Object>> handleNotFound(RuntimeException ex) {
        ApiResponse<Object> body = ApiResponse
                .error(ex.getMessage() == null ? "Resource not found" : ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler({ DuplicateResourceException.class, AppointmentConflictException.class })
    protected ResponseEntity<ApiResponse<Object>> handleConflict(RuntimeException ex) {
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({ BusinessRuleException.class })
    protected ResponseEntity<ApiResponse<Object>> handleBusinessRule(RuntimeException ex) {
        ApiResponse<Object> body = ApiResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String msg = (cause != null && cause.getMessage() != null) ? cause.getMessage() : 
                     (ex.getMessage() != null ? ex.getMessage() : "Integrity violation");
        ApiResponse<Object> body = ApiResponse.error("Integrity violation: " + msg);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@Nullable HttpMessageNotReadableException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {
        String message = (ex != null && ex.getMessage() != null) ? ex.getMessage() : "Invalid request body";
        ApiResponse<Object> body = ApiResponse.error("Invalid or malformed request body: " + message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@Nullable MethodArgumentNotValidException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {
        String errors = "";
        if (ex != null) {
            errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("");
        }

        ApiResponse<Object> body = ApiResponse.error("Validation errors: " + errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String typeName = (ex.getRequiredType() != null) ? ex.getRequiredType().getSimpleName() : "correct type";
        String msg = String.format("Invalid parameter '%s': value='%s'. Expected: %s",
                ex.getName(), ex.getValue(), typeName);
        ApiResponse<Object> body = ApiResponse.error(msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@Nullable HttpMediaTypeNotSupportedException ex,
            @Nullable HttpHeaders headers,
            @Nullable HttpStatusCode status,
            @Nullable WebRequest request) {
        String contentType = (ex != null && ex.getContentType() != null) ? ex.getContentType().toString() : "unknown";
        ApiResponse<Object> body = ApiResponse.error("Unsupported media type: " + contentType);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<ApiResponse<Object>> handleAll(Exception ex) {
        ApiResponse<Object> body = ApiResponse
                .error("Internal server error: " + (ex.getMessage() != null ? ex.getMessage() : ""));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
