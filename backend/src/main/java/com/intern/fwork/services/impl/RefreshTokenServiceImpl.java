package com.intern.fwork.services.impl;

import com.intern.fwork.entities.RefreshToken;
import com.intern.fwork.entities.User;
import com.intern.fwork.repositories.RefreshTokenRepository;
import com.intern.fwork.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public RefreshToken create(User user) {
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(UUID.randomUUID().toString())
                        .user(user)
                        .expiryDate(
                                Instant.now().plus(30, ChronoUnit.DAYS)
                        )
                        .revoked(false)
                        .build();

        return refreshTokenRepository.save(refreshToken);

    }

    @Override
    public RefreshToken verify(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }


    @Override
    public void revoke(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Token not found"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
    @Override
    public void revokeAll(User user) {

    }
}
