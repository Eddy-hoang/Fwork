package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.UpdateProfileRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.UserResponse;
import com.intern.fwork.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(
                authService.getCurrentUser()
        );
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(
                authService.updateProfile(request)
        );
    }

    @GetMapping("/search")
    public ApiResponse<java.util.List<UserResponse>> search(@RequestParam String q) {
        return ApiResponse.success(
                authService.searchUsers(q)
        );
    }
}
