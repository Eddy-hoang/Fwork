package com.intern.fwork.events;

import com.intern.fwork.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Listens to Spring Security authentication events and writes audit records.
 *
 * Note: These events fire OUTSIDE a DB transaction, so AuditLogService uses
 * REQUIRES_NEW propagation to create its own independent transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityAuditListener {

    private final AuditLogService auditLogService;

    @Async
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            if (event.getAuthentication() != null) {
                String username = event.getAuthentication().getName();
                auditLogService.log(
                        null,
                        username,
                        "LOGIN_SUCCESS",
                        null,
                        null,
                        "User authenticated successfully",
                        null
                );
            }
        } catch (Exception ex) {
            log.warn("Failed to write login success audit log: {}", ex.getMessage());
        }
    }

    @Async
    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        try {
            if (event.getAuthentication() != null) {
                String username = event.getAuthentication().getName();
                String reason = event.getException() != null ? event.getException().getMessage() : "Unknown";
                auditLogService.log(
                        null,
                        username,
                        "LOGIN_FAILURE",
                        null,
                        null,
                        "Authentication failed: " + reason,
                        null
                );
            }
        } catch (Exception ex) {
            log.warn("Failed to write login failure audit log: {}", ex.getMessage());
        }
    }
}
