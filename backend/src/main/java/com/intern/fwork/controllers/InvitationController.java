package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.SendInvitationRequest;
import com.intern.fwork.dtos.request.TransferOwnershipRequest;
import com.intern.fwork.dtos.request.UpdateMemberRoleRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.InvitationResponse;
import com.intern.fwork.services.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;

    /**
     * POST /api/workspaces/{workspaceId}/invitations
     * OWNER or ADMIN sends an invitation
     */
    @PostMapping("/{workspaceId}/invitations")
    public ResponseEntity<ApiResponse<InvitationResponse>> sendInvitation(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody SendInvitationRequest request
    ) {
        InvitationResponse response = invitationService.sendInvitation(workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Invitation sent"));
    }

    /**
     * POST /api/invitations/accept?token=...
     * Accept an invitation via plaintext token (typically from an email link)
     */
    @PostMapping({"/invitations/accept", "/api/invitations/accept"})
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @RequestParam String token
    ) {
        invitationService.acceptInvitation(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Invitation accepted — you are now a workspace member"));
    }

    /**
     * PATCH /api/workspaces/{workspaceId}/members/role
     * OWNER or ADMIN updates a member's role
     */
    @PatchMapping("/{workspaceId}/members/role")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        invitationService.updateMemberRole(workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Member role updated"));
    }

    /**
     * POST /api/workspaces/{workspaceId}/transfer-ownership
     * OWNER transfers ownership to another member
     */
    @PostMapping("/{workspaceId}/transfer-ownership")
    public ResponseEntity<ApiResponse<Void>> transferOwnership(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody TransferOwnershipRequest request
    ) {
        invitationService.transferOwnership(workspaceId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Ownership transferred"));
    }
}
