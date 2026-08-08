package com.intern.fwork.utils;

import com.intern.fwork.entities.User;
import com.intern.fwork.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new RuntimeException("User not authenticated");
        }

        return userDetails.getUser();
    }
}