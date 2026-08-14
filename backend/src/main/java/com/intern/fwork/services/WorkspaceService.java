package com.intern.fwork.services;

import com.intern.fwork.dtos.request.AddMemberRequest;
import com.intern.fwork.dtos.request.CreateWorkspaceRequest;
import com.intern.fwork.dtos.request.UpdateWorkspaceRequest;
import com.intern.fwork.dtos.response.WorkspaceMemberResponse;
import com.intern.fwork.dtos.response.WorkspaceResponse;

import java.util.List;
import java.util.UUID;

public interface WorkspaceService {

    WorkspaceResponse create(CreateWorkspaceRequest request);

    List<WorkspaceResponse> getMyWorkspaces();

    WorkspaceResponse getById(UUID id);

    WorkspaceResponse update(UUID id, UpdateWorkspaceRequest request);

    void delete(UUID id);

    void addMember(UUID workspaceId, AddMemberRequest request);

    void removeMember(UUID workspaceId, UUID userId);

    List<WorkspaceMemberResponse> getMembers(UUID workspaceId);

    com.intern.fwork.dtos.response.WorkspaceCacheDto getWorkspaceCacheData(UUID id);
}
