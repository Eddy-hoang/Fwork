package com.intern.fwork.exceptions;

import com.intern.fwork.dtos.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Bat loi 404(Khong tim thay du lieu)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=",""))
                .build();
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Bat toan bo loi Runtime chua duoc dinh nghia 500
    // Tranh crash app hoặc lộ câu lệnh SQL ra ngoài Frontend
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handGlobalException(
            Exception ex, WebRequest request
    ){
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Lỗi cục bộ")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=",""))
                .build();
        return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
