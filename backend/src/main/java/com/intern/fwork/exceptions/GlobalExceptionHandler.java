package com.intern.fwork.exceptions;

import com.intern.fwork.dtos.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bat loi 404 (Khong tim thay du lieu)
    @ExceptionHandler({
            ResourceNotFoundException.class,
            WorkspaceNotFoundException.class,
            BoardNotFoundException.class,
            UserNotFoundException.class,
            BoardColumnNotFoundException.class,
            TaskNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleNotFoundExceptions(
            RuntimeException ex, WebRequest request
    ) {
        ApiResponse<Object> error = ApiResponse.error(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Bat loi 403 (Tu choi truy cap)
    @ExceptionHandler({
            AccessDeniedException.class,
            ForbiddenOperationException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleForbiddenExceptions(
            Exception ex, WebRequest request
    ) {
        ApiResponse<Object> error = ApiResponse.error(HttpStatus.FORBIDDEN.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    // Bat loi 400 (Bad Request)
    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(
            Exception ex, WebRequest request
    ) {
        ApiResponse<Object> error = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Bat loi 409 (Conflict)
    @ExceptionHandler({
            DuplicateResourceException.class,
            MemberAlreadyExistsException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleConflictExceptions(
            RuntimeException ex,
            WebRequest request
    ) {
        ApiResponse<Object> error = ApiResponse.error(HttpStatus.CONFLICT.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Bat loi thieu Request Body hoac JSON khong hop le (400 Bad Request)
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(
            org.springframework.http.converter.HttpMessageNotReadableException ex, WebRequest request
    ) {
        ApiResponse<Object> error = ApiResponse.error(HttpStatus.BAD_REQUEST.value(),
                "Request body is missing or invalid: " + ex.getMostSpecificCause().getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Bat loi validation du lieu dau vao @Valid (400 Bad Request)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
            org.springframework.web.bind.MethodArgumentNotValidException ex, WebRequest request
    ) {
        Map<String, String> errorsMap = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        ApiResponse<Object> error = ApiResponse.error(HttpStatus.BAD_REQUEST.value(), "Validation failed", errorsMap);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Bat loi database constraint violation (409 Conflict)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, WebRequest request
    ) {
        ApiResponse<Object> error = ApiResponse.error(HttpStatus.CONFLICT.value(), "Database constraint violation");
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Bat loi Authentication (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request
    ) {
        ApiResponse<Object> error = ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // Bat toan bo loi Runtime chua duoc dinh nghia 500
    // Tranh crash app hoặc lộ câu lệnh SQL ra ngoài Frontend
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handGlobalException(
            Exception ex, WebRequest request
    ){
        // Log details locally
        ex.printStackTrace();

        ApiResponse<Object> error = ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
