package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.SendInvitationRequest;
import com.intern.fwork.dtos.request.TransferOwnershipRequest;
import com.intern.fwork.dtos.request.UpdateMemberRoleRequest;
import com.intern.fwork.dtos.response.InvitationResponse;
import com.intern.fwork.entities.User;
import com.intern.fwork.entities.Workspace;
import com.intern.fwork.entities.WorkspaceInvitation;
import com.intern.fwork.entities.WorkspaceMember;
import com.intern.fwork.enums.InvitationStatus;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.exceptions.BadRequestException;
import com.intern.fwork.exceptions.DuplicateResourceException;
import com.intern.fwork.exceptions.ForbiddenOperationException;
import com.intern.fwork.exceptions.ResourceNotFoundException;
import com.intern.fwork.repositories.UserRepository;
import com.intern.fwork.repositories.WorkspaceInvitationRepository;
import com.intern.fwork.repositories.WorkspaceMemberRepository;
import com.intern.fwork.repositories.WorkspaceRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.AuditLogService;
import com.intern.fwork.services.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private static final int TOKEN_BYTES = 32;
    private static final int INVITATION_EXPIRY_DAYS = 7;

    private final WorkspaceInvitationRepository invitationRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final AuditLogService auditLogService;

    // ──────────────────────────────────────────────────────────────────────────
    // Send Invitation
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InvitationResponse sendInvitation(UUID workspaceId, SendInvitationRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        WorkspaceRole callerRole = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, currentUser.getId())
                .map(WorkspaceMember::getRole)
                .orElseThrow(() -> new AccessDeniedException("Not a member of this workspace"));

        // Only OWNER can invite ADMINs; ADMIN can only invite MEMBERs
        if (callerRole == WorkspaceRole.MEMBER) {
            throw new AccessDeniedException("Members cannot send invitations");
        }
        if (callerRole == WorkspaceRole.ADMIN && request.getRole() != WorkspaceRole.MEMBER) {
            throw new ForbiddenOperationException("ADMINs can only invite MEMBER-role users");
        }
        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("Cannot invite someone directly as OWNER — use transfer ownership");
        }

        String email = request.getEmail().toLowerCase().trim();

        // Check for duplicate PENDING invitation
        if (invitationRepository.existsByWorkspaceIdAndEmailAndStatus(workspaceId, email, InvitationStatus.PENDING)) {
            throw new DuplicateResourceException("A pending invitation already exists for " + email);
        }

        // Generate plaintext token and hash it
        String plainToken = generateToken();
        String tokenHash = sha256(plainToken);

        WorkspaceInvitation invitation = WorkspaceInvitation.builder()
                .workspace(workspace)
                .email(email)
                .tokenHash(tokenHash)
                .role(request.getRole())
                .status(InvitationStatus.PENDING)
                .invitedBy(currentUser)
                .expiresAt(LocalDateTime.now().plusDays(INVITATION_EXPIRY_DAYS))
                .build();

        invitation = invitationRepository.save(invitation);

        // In production: send plainToken via email. Here we return the response.
        // The plain token is NOT stored or returned in the response for security.
        return toResponse(invitation);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Accept Invitation — single transaction
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void acceptInvitation(String token) {
        String tokenHash = sha256(token);
        WorkspaceInvitation invitation = invitationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or already used"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Invitation is no longer pending (status: " + invitation.getStatus() + ")");
        }
        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BadRequestException("Invitation has expired");
        }

        String email = invitation.getEmail();
        User invitee = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user account found for " + email + ". Please register first."));

        UUID workspaceId = invitation.getWorkspace().getId();

        // Prevent duplicate membership
        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, invitee.getId()).isPresent()) {
            throw new DuplicateResourceException("User is already a member of this workspace");
        }

        // Mark invitation accepted
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        // Add member
        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(invitation.getWorkspace())
                .user(invitee)
                .role(invitation.getRole())
                .build();
        workspaceMemberRepository.save(member);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Update Member Role
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void updateMemberRole(UUID workspaceId, UpdateMemberRoleRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        WorkspaceMember callerMembership = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Not a member of this workspace"));

        WorkspaceRole callerRole = callerMembership.getRole();

        WorkspaceMember targetMembership = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this workspace"));

        // OWNER cannot demote themselves or be demoted
        if (targetMembership.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("Cannot change the role of the OWNER — use transfer ownership");
        }

        // Cannot assign OWNER role directly
        if (request.getRole() == WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("Cannot assign OWNER role directly — use transfer ownership");
        }

        // ADMIN can only manage MEMBER roles
        if (callerRole == WorkspaceRole.ADMIN) {
            if (targetMembership.getRole() != WorkspaceRole.MEMBER || request.getRole() != WorkspaceRole.MEMBER) {
                throw new ForbiddenOperationException("ADMINs can only manage MEMBER-role users");
            }
        }

        if (callerRole == WorkspaceRole.MEMBER) {
            throw new AccessDeniedException("MEMBERs cannot manage roles");
        }

        targetMembership.setRole(request.getRole());
        workspaceMemberRepository.save(targetMembership);

        auditLogService.log(
                currentUser.getId(),
                currentUser.getEmail(),
                "ROLE_CHANGED",
                "WORKSPACE",
                workspaceId,
                "User " + request.getUserId() + " role changed to " + request.getRole(),
                null
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Transfer Ownership
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void transferOwnership(UUID workspaceId, TransferOwnershipRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        WorkspaceMember callerMembership = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Not a member of this workspace"));

        if (callerMembership.getRole() != WorkspaceRole.OWNER) {
            throw new ForbiddenOperationException("Only the OWNER can transfer ownership");
        }

        if (currentUser.getId().equals(request.getNewOwnerId())) {
            throw new BadRequestException("Cannot transfer ownership to yourself");
        }

        WorkspaceMember newOwnerMembership = workspaceMemberRepository
                .findByWorkspaceIdAndUserId(workspaceId, request.getNewOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("New owner is not a member of this workspace"));

        // Demote current owner to ADMIN, promote new owner
        callerMembership.setRole(WorkspaceRole.ADMIN);
        newOwnerMembership.setRole(WorkspaceRole.OWNER);

        workspaceMemberRepository.save(callerMembership);
        workspaceMemberRepository.save(newOwnerMembership);

        auditLogService.log(
                currentUser.getId(),
                currentUser.getEmail(),
                "OWNER_TRANSFERRED",
                "WORKSPACE",
                workspaceId,
                "Ownership transferred to userId " + request.getNewOwnerId(),
                null
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private InvitationResponse toResponse(WorkspaceInvitation inv) {
        return InvitationResponse.builder()
                .id(inv.getId())
                .workspaceId(inv.getWorkspace().getId())
                .workspaceName(inv.getWorkspace().getName())
                .email(inv.getEmail())
                .role(inv.getRole())
                .status(inv.getStatus())
                .expiresAt(inv.getExpiresAt())
                .createdAt(inv.getCreatedAt())
                .build();
    }
}
