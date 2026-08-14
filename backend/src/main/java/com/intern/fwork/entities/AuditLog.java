package com.intern.fwork.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit log — never update or delete these records.
 * Tracks security-relevant actions: logins, failures, role changes, etc.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_logs_created_at", columnList = "created_at"),
        @Index(name = "idx_audit_logs_action", columnList = "action")
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The actor (null for anonymous requests such as failed logins) */
    @Column(name = "user_id")
    private UUID userId;

    /** Human-readable username/email for tracing even if user is later deleted */
    @Column(name = "username", length = 255)
    private String username;

    /** Action category: LOGIN_SUCCESS, LOGIN_FAILURE, ROLE_CHANGED, OWNER_TRANSFERRED, etc. */
    @Column(nullable = false, length = 64)
    private String action;

    /** Resource type affected, e.g. WORKSPACE, BOARD */
    @Column(name = "resource_type", length = 64)
    private String resourceType;

    /** Resource ID affected */
    @Column(name = "resource_id")
    private UUID resourceId;

    /** Free-form detail — MUST NOT contain passwords or other secrets */
    @Column(length = 1024)
    private String detail;

    /** Client IP address */
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
