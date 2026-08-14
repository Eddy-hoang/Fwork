package com.intern.fwork.repositories;

import com.intern.fwork.entities.WorkspaceInvitation;
import com.intern.fwork.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceInvitationRepository extends JpaRepository<WorkspaceInvitation, UUID> {

    Optional<WorkspaceInvitation> findByTokenHash(String tokenHash);

    /**
     * Check if a PENDING invitation already exists for this (workspace, email) pair.
     */
    boolean existsByWorkspaceIdAndEmailAndStatus(UUID workspaceId, String email, InvitationStatus status);
}
