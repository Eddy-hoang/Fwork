package com.intern.fwork.services.impl;

import com.intern.fwork.entities.RefreshToken;
import com.intern.fwork.entities.User;
import com.intern.fwork.exceptions.ResourceNotFoundException;
import com.intern.fwork.repositories.RefreshTokenRepository;
import com.intern.fwork.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    public RefreshToken create(User user) {
        UUID familyId = UUID.randomUUID();
        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(UUID.randomUUID().toString())
                        .user(user)
                        .familyId(familyId)
                        .expiryDate(
                                Instant.now().plusMillis(refreshExpiration)
                        )
                        .revoked(false)
                        .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken rotate(String token) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        User user = oldToken.getUser();

        // REPLAY / REUSE DETECTION:
        // If an already revoked token is submitted, assume compromise and revoke all sessions for this user.
        if (oldToken.isRevoked()) {
            revokeAll(user);
            throw new org.springframework.security.access.AccessDeniedException(
                    "Refresh token reuse detected. All user sessions have been revoked."
            );
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            throw new org.springframework.security.access.AccessDeniedException("Refresh token expired");
        }

        // Atomically revoke the consumed token
        oldToken.setRevoked(true);

        UUID familyId = oldToken.getFamilyId() != null ? oldToken.getFamilyId() : UUID.randomUUID();
        oldToken.setFamilyId(familyId);

        String newTokenString = UUID.randomUUID().toString();
        oldToken.setReplacedBy(newTokenString);
        refreshTokenRepository.save(oldToken);

        RefreshToken newToken = RefreshToken.builder()
                .token(newTokenString)
                .user(user)
                .familyId(familyId)
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(newToken);
    }

    @Override
    public RefreshToken verify(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new org.springframework.security.access.AccessDeniedException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new org.springframework.security.access.AccessDeniedException("Refresh token expired");
        }

        return refreshToken;
    }


    @Override
    public void revoke(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Token not found"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAll(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(user);
        for (RefreshToken token : tokens) {
            if (!token.isRevoked()) {
                token.setRevoked(true);
            }
        }
        refreshTokenRepository.saveAll(tokens);
    }
}
