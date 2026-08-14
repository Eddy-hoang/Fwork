package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.AddMemberRequest;
import com.intern.fwork.dtos.request.CreateWorkspaceRequest;
import com.intern.fwork.dtos.request.UpdateWorkspaceRequest;
import com.intern.fwork.dtos.response.WorkspaceMemberResponse;
import com.intern.fwork.dtos.response.WorkspaceResponse;
import com.intern.fwork.entities.User;
import com.intern.fwork.entities.Workspace;
import com.intern.fwork.entities.WorkspaceMember;
import com.intern.fwork.enums.WorkspaceRole;
import com.intern.fwork.exceptions.*;
import com.intern.fwork.mappers.WorkspaceMapper;
import com.intern.fwork.repositories.BoardRepository;
import com.intern.fwork.repositories.UserRepository;
import com.intern.fwork.repositories.WorkspaceMemberRepository;
import com.intern.fwork.repositories.WorkspaceRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.PermissionService;
import com.intern.fwork.services.WorkspaceService;
import com.intern.fwork.dtos.response.WorkspaceCacheDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final WorkspaceMapper workspaceMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private WorkspaceService self;

    @Override
    public WorkspaceResponse create(CreateWorkspaceRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        String slug = generateUniqueSlug(request.getName());

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .isArchived(false)
                .createdBy(currentUser)
                .updatedBy(currentUser)
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember member = WorkspaceMember.builder()
                .workspace(savedWorkspace)
                .user(currentUser)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceMemberRepository.save(member);

        WorkspaceResponse response = workspaceMapper.toResponse(savedWorkspace);
        response.setMemberCount(1);
        response.setBoardCount(0);
        response.setCurrentUserRole(WorkspaceRole.OWNER);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces() {
        User currentUser = securityUtils.getCurrentUser();

        List<WorkspaceMember> memberships = workspaceMemberRepository.findByUserId(currentUser.getId());

        return memberships.stream()
                .map(membership -> {
                    Workspace workspace = membership.getWorkspace();
                    if (workspace.isArchived()) {
                        return null;
                    }
                    WorkspaceResponse response = workspaceMapper.toResponse(workspace);
                    response.setMemberCount(workspaceMemberRepository.countByWorkspaceId(workspace.getId()));
                    response.setBoardCount(boardRepository.countByWorkspaceIdAndIsArchivedFalse(workspace.getId()));
                    response.setCurrentUserRole(membership.getRole());
                    return response;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceResponse getById(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        permissionService.checkReadWorkspace(id, currentUser.getId());

        WorkspaceCacheDto cacheDto = self.getWorkspaceCacheData(id);

        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Access Denied"));

        return WorkspaceResponse.builder()
                .id(cacheDto.getId())
                .name(cacheDto.getName())
                .slug(cacheDto.getSlug())
                .description(cacheDto.getDescription())
                .memberCount(cacheDto.getMemberCount())
                .boardCount(cacheDto.getBoardCount())
                .currentUserRole(membership.getRole())
                .createdAt(cacheDto.getCreatedAt())
                .updatedAt(cacheDto.getUpdatedAt())
                .createdBy(cacheDto.getCreatedBy())
                .updatedBy(cacheDto.getUpdatedBy())
                .build();
    }

    @Override
    @CacheEvict(value = "workspace", key = "#id")
    public WorkspaceResponse update(UUID id, UpdateWorkspaceRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Workspace workspace = workspaceRepository.findById(id)
                .filter(w -> !w.isArchived())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        permissionService.checkEditWorkspace(id, currentUser.getId());

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setUpdatedBy(currentUser);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Access Denied"));

        WorkspaceResponse response = workspaceMapper.toResponse(savedWorkspace);
        response.setMemberCount(workspaceMemberRepository.countByWorkspaceId(id));
        response.setBoardCount(boardRepository.countByWorkspaceIdAndIsArchivedFalse(id));
        response.setCurrentUserRole(membership.getRole());

        return response;
    }

    @Override
    @CacheEvict(value = "workspace", key = "#id")
    public void delete(UUID id) {
        User currentUser = securityUtils.getCurrentUser();

        Workspace workspace = workspaceRepository.findById(id)
                .filter(w -> !w.isArchived())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        permissionService.checkDeleteWorkspace(id, currentUser.getId());

        workspace.setArchived(true);
        workspace.setUpdatedBy(currentUser);
        workspaceRepository.save(workspace);
    }

    @Override
    @CacheEvict(value = "workspace", key = "#workspaceId")
    public void addMember(UUID workspaceId, AddMemberRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .filter(w -> !w.isArchived())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        permissionService.checkManageMembers(workspaceId, currentUser.getId());

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    String email = request.getEmail();
                    String name = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .passwordHash(passwordEncoder.encode("Fwork@123456"))
                            .role(com.intern.fwork.enums.Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });

        boolean alreadyMember = workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, targetUser.getId());
        if (alreadyMember) {
            throw new MemberAlreadyExistsException("User is already a member of this workspace");
        }

        WorkspaceMember newMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(targetUser)
                .role(request.getRole())
                .build();

        workspaceMemberRepository.save(newMember);
    }

    @Override
    @CacheEvict(value = "workspace", key = "#workspaceId")
    public void removeMember(UUID workspaceId, UUID userId) {
        User currentUser = securityUtils.getCurrentUser();

        permissionService.checkRemoveMember(workspaceId, currentUser.getId(), userId);

        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in workspace"));

        workspaceMemberRepository.delete(targetMember);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkspaceMemberResponse> getMembers(UUID workspaceId) {
        User currentUser = securityUtils.getCurrentUser();

        permissionService.checkReadWorkspace(workspaceId, currentUser.getId());

        List<WorkspaceMember> members = workspaceMemberRepository.findByWorkspaceId(workspaceId);

        return members.stream()
                .map(m -> WorkspaceMemberResponse.builder()
                        .id(m.getId())
                        .userId(m.getUser().getId())
                        .name(m.getUser().getName())
                        .email(m.getUser().getEmail())
                        .avatar(m.getUser().getAvatar())
                        .role(m.getRole())
                        .joinedAt(m.getJoinedAt())
                        .build())
                .toList();
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
        if (baseSlug.isEmpty()) {
            baseSlug = "workspace";
        }
        String slug = baseSlug;
        int count = 1;
        while (workspaceRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + count;
            count++;
        }
        return slug;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "workspace", key = "#id")
    public WorkspaceCacheDto getWorkspaceCacheData(UUID id) {
        Workspace workspace = workspaceRepository.findById(id)
                .filter(w -> !w.isArchived())
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        long memberCount = workspaceMemberRepository.countByWorkspaceId(id);
        long boardCount = boardRepository.countByWorkspaceIdAndIsArchivedFalse(id);

        return WorkspaceCacheDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .slug(workspace.getSlug())
                .description(workspace.getDescription())
                .memberCount(memberCount)
                .boardCount(boardCount)
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .createdBy(workspace.getCreatedBy() != null ? workspace.getCreatedBy().getId() : null)
                .updatedBy(workspace.getUpdatedBy() != null ? workspace.getUpdatedBy().getId() : null)
                .build();
    }
}
