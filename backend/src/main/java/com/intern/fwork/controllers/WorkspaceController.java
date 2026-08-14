package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.AddMemberRequest;
import com.intern.fwork.dtos.request.CreateBoardRequest;
import com.intern.fwork.dtos.request.CreateWorkspaceRequest;
import com.intern.fwork.dtos.request.UpdateWorkspaceRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.BoardResponse;
import com.intern.fwork.dtos.response.WorkspaceMemberResponse;
import com.intern.fwork.dtos.response.WorkspaceResponse;
import com.intern.fwork.services.BoardService;
import com.intern.fwork.services.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final BoardService boardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkspaceResponse> create(@Valid @RequestBody CreateWorkspaceRequest request) {
        return ApiResponse.success(workspaceService.create(request));
    }

    @GetMapping
    public ApiResponse<List<WorkspaceResponse>> getMyWorkspaces() {
        return ApiResponse.success(workspaceService.getMyWorkspaces());
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkspaceResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(workspaceService.getById(id));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<WorkspaceResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorkspaceRequest request
    ) {
        return ApiResponse.success(workspaceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        workspaceService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{workspaceId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> addMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody AddMemberRequest request
    ) {
        workspaceService.addMember(workspaceId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ApiResponse<Void> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId
    ) {
        workspaceService.removeMember(workspaceId, userId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{workspaceId}/members")
    public ApiResponse<List<WorkspaceMemberResponse>> getMembers(@PathVariable UUID workspaceId) {
        return ApiResponse.success(workspaceService.getMembers(workspaceId));
    }

    @GetMapping("/{workspaceId}/boards")
    public ApiResponse<List<BoardResponse>> getBoards(@PathVariable UUID workspaceId) {
        return ApiResponse.success(boardService.getBoardsByWorkspaceId(workspaceId));
    }

    @PostMapping("/{workspaceId}/boards")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BoardResponse> createBoardInWorkspace(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateBoardRequest request
    ) {
        request.setWorkspaceId(workspaceId);
        return ApiResponse.success(boardService.create(request));
    }
}
