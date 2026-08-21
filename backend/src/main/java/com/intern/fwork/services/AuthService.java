package com.intern.fwork.services;

import com.intern.fwork.dtos.request.LoginRequest;
import com.intern.fwork.dtos.request.RefreshTokenRequest;
import com.intern.fwork.dtos.request.RegisterRequest;
import com.intern.fwork.dtos.request.UpdateProfileRequest;
import com.intern.fwork.dtos.response.LoginResponse;
import com.intern.fwork.dtos.response.RefreshTokenResponse;
import com.intern.fwork.dtos.response.UserResponse;
import jakarta.validation.Valid;

public interface AuthService {

    LoginResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(RefreshTokenRequest request);

    UserResponse getCurrentUser();

    RefreshTokenResponse refresh(@Valid RefreshTokenRequest request);

    LoginResponse loginWithGoogle(String idToken);

    java.util.List<UserResponse> searchUsers(String query);

    UserResponse updateProfile(UpdateProfileRequest request);

}
