package com.intern.fwork.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)//Bo qua truong hop null khi chuyen qua json
public class ApiResponse<T> {

    private boolean success;

    private int status;

    private String message;

    private T data;

    private LocalDateTime localDateTime;

    private Object errors;

    public static <T> ApiResponse<T> success(T data){
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message("Success")
                .data(data)
                .localDateTime(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message){
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .localDateTime(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message){
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .localDateTime(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, Object errors){
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors)
                .localDateTime(LocalDateTime.now())
                .build();
    }
}
