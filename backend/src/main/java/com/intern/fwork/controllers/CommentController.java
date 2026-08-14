package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.CreateCommentRequest;
import com.intern.fwork.dtos.request.UpdateCommentRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.CommentResponse;
import com.intern.fwork.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/api/tasks/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> create(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ApiResponse.success(commentService.create(taskId, request));
    }

    @GetMapping("/api/tasks/{taskId}/comments")
    public ApiResponse<org.springframework.data.domain.Page<CommentResponse>> getByTask(
            @PathVariable UUID taskId,
            @org.springframework.data.web.PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.ASC) org.springframework.data.domain.Pageable pageable
    ) {
        return ApiResponse.success(commentService.getCommentsByTask(taskId, pageable));
    }

    @PutMapping("/api/comments/{id}")
    public ApiResponse<CommentResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        return ApiResponse.success(commentService.update(id, request));
    }

    @DeleteMapping("/api/comments/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        commentService.delete(id);
        return ApiResponse.success(null);
    }
}
