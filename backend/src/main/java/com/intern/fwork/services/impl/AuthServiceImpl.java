package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.LoginRequest;
import com.intern.fwork.dtos.request.RefreshTokenRequest;
import com.intern.fwork.dtos.request.RegisterRequest;
import com.intern.fwork.dtos.request.UpdateProfileRequest;
import com.intern.fwork.dtos.response.LoginResponse;
import com.intern.fwork.dtos.response.RefreshTokenResponse;
import com.intern.fwork.dtos.response.UserResponse;
import com.intern.fwork.entities.RefreshToken;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.Role;
import com.intern.fwork.exceptions.DuplicateResourceException;
import com.intern.fwork.mappers.UserMapper;
import com.intern.fwork.repositories.UserRepository;
import com.intern.fwork.security.CustomUserDetails;
import com.intern.fwork.security.JwtService;
import com.intern.fwork.services.AuthService;
import com.intern.fwork.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserResponse register(RegisterRequest request) {

        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User {} registered successfully", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        String token = jwtService.generateToken(userDetails);

        log.info("User {} logged in successfully", user.getEmail());

        RefreshToken refreshToken = refreshTokenService.create(user);

        return LoginResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration() / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout(RefreshTokenRequest request) {

        refreshTokenService.revoke(
                request.getRefreshToken()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }

        return userMapper.toResponse(
                userDetails.getUser()
        );
    }

    @Override
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request
    ) {

        RefreshToken refreshToken =
                refreshTokenService.verify(
                        request.getRefreshToken()
                );

        User user = refreshToken.getUser();

        CustomUserDetails userDetails =
                new CustomUserDetails(user);

        String accessToken =
                jwtService.generateToken(userDetails);

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration() / 1000)
                .build();
    }

    @Override
    public LoginResponse loginWithGoogle(String idToken) {
        log.info("Attempting Google login");
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Google ID token is required");
        }

        try {
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            org.springframework.http.ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> payload = responseEntity.getBody();

            if (payload == null || payload.containsKey("error_description")) {
                throw new org.springframework.security.access.AccessDeniedException("Invalid Google token");
            }

            String email = (String) payload.get("email");
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            if (email == null || email.isBlank()) {
                throw new org.springframework.security.access.AccessDeniedException("Email not provided by Google");
            }

            java.util.Optional<User> userOpt = userRepository.findByEmail(email);
            User user;

            if (userOpt.isPresent()) {
                user = userOpt.get();
                if (name != null && !name.isBlank()) {
                    user.setName(name);
                }
                if (picture != null && !picture.isBlank()) {
                    user.setAvatar(picture);
                }
                user = userRepository.save(user);
            } else {
                user = User.builder()
                        .name(name != null ? name : email.split("@")[0])
                        .email(email)
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .role(Role.USER)
                        .avatar(picture)
                        .build();
                user = userRepository.save(user);
                log.info("Created new user via Google login: {}", email);
            }

            CustomUserDetails userDetails = new CustomUserDetails(user);
            String token = jwtService.generateToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.create(user);

            return LoginResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getJwtExpiration() / 1000)
                    .user(userMapper.toResponse(user))
                    .build();

        } catch (Exception e) {
            log.error("Google authentication failed: {}", e.getMessage(), e);
            throw new org.springframework.security.access.AccessDeniedException("Google authentication failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserResponse> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        String q = query.trim();
        return userRepository.findByEmailContainingIgnoreCaseOrNameContainingIgnoreCase(q, q)
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public UserResponse updateProfile(UpdateProfileRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }
        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not found"));
        user.setName(request.getName());
        user.setAvatar(request.getAvatar());
        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }
}