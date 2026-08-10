package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.LoginRequest;
import com.intern.fwork.dtos.request.RefreshTokenRequest;
import com.intern.fwork.dtos.request.RegisterRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.LoginResponse;
import com.intern.fwork.dtos.response.RefreshTokenResponse;
import com.intern.fwork.dtos.response.UserResponse;
import com.intern.fwork.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody RegisterRequest request
    ){
        UserResponse response = authService.register(request);

        return ApiResponse.success(response);
    }


    @PostMapping("/login")
    public ApiResponse<LoginResponse> loginn(
            @Valid
            @RequestBody
            LoginRequest request
    ){
        LoginResponse response = authService.login(request);

        return ApiResponse.success(response);
    }

    @PostMapping("/me")
    public ApiResponse<UserResponse> me(){
        return ApiResponse.success(
                authService.getCurrentUser()
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refresh(
            @Valid
            @RequestBody
            RefreshTokenRequest request
    ){
        return ApiResponse.success(
                authService.refresh(request)
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestBody RefreshTokenRequest request
    ) {
        authService.logout(request);

        return ApiResponse.success(null);
    }
}
