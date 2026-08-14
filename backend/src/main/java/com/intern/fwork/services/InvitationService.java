package com.intern.fwork.services;

import com.intern.fwork.dtos.request.SendInvitationRequest;
import com.intern.fwork.dtos.request.TransferOwnershipRequest;
import com.intern.fwork.dtos.request.UpdateMemberRoleRequest;
import com.intern.fwork.dtos.response.InvitationResponse;

import java.util.UUID;

public interface InvitationService {

    /** OWNER or ADMIN sends an invitation to join a workspace */
    InvitationResponse sendInvitation(UUID workspaceId, SendInvitationRequest request);

    /**
     * Accept an invitation by its plaintext token.
     * Runs in a single transaction: mark accepted + add member.
     */
    void acceptInvitation(String token);

    /** OWNER updates a member's role; ADMIN can only update MEMBER role */
    void updateMemberRole(UUID workspaceId, UpdateMemberRoleRequest request);

    /** Transfer OWNER role to another member — only current OWNER can do this */
    void transferOwnership(UUID workspaceId, TransferOwnershipRequest request);
}
