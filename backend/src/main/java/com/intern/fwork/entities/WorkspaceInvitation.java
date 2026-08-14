package com.intern.fwork.entities;

import com.intern.fwork.enums.InvitationStatus;
import com.intern.fwork.enums.WorkspaceRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores workspace invitations with a SHA-256 hashed token.
 * Unique constraint on (workspace_id, email) for PENDING invitations
 * is enforced at the application level (see InvitationServiceImpl).
 */
@Entity
@Table(
    name = "workspace_invitations",
    indexes = {
        @Index(name = "idx_invitations_token_hash", columnList = "token_hash"),
        @Index(name = "idx_invitations_workspace_email", columnList = "workspace_id, email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    /** Email of the person being invited (may not be a user yet) */
    @Column(nullable = false, length = 255)
    private String email;

    /** SHA-256 hash of the plaintext token (never store plaintext) */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** The role that will be granted on acceptance */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", nullable = false)
    private User invitedBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = InvitationStatus.PENDING;
    }
}
