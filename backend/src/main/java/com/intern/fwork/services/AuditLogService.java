package com.intern.fwork.services;

import com.intern.fwork.entities.AuditLog;

import java.util.UUID;

public interface AuditLogService {

    /**
     * Record an audit event. This should be called from @Async listeners to avoid
     * blocking the main request thread.
     *
     * @param userId       ID of the actor (null for anonymous)
     * @param username     Email/name of the actor (for historical tracing)
     * @param action       Action category (e.g. LOGIN_SUCCESS, ROLE_CHANGED)
     * @param resourceType Resource type (e.g. WORKSPACE, BOARD) — may be null
     * @param resourceId   Resource ID — may be null
     * @param detail       Human-readable detail — must NOT contain PII/secrets
     * @param ipAddress    Client IP — may be null
     */
    void log(UUID userId, String username, String action,
             String resourceType, UUID resourceId,
             String detail, String ipAddress);
}
