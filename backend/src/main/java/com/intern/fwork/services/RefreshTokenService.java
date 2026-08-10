package com.intern.fwork.services;

import com.intern.fwork.entities.RefreshToken;
import com.intern.fwork.entities.User;

public interface RefreshTokenService {

    RefreshToken create(User user);

    RefreshToken verify(String token);

    void revoke(String token);

    void revokeAll(User user);
}
