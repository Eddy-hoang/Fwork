package com.intern.fwork.services;

import com.intern.fwork.dtos.request.CreateCommentRequest;
import com.intern.fwork.dtos.request.UpdateCommentRequest;
import com.intern.fwork.dtos.response.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentResponse create(UUID taskId, CreateCommentRequest request);

    List<CommentResponse> getCommentsByTask(UUID taskId);

    CommentResponse update(UUID commentId, UpdateCommentRequest request);

    void delete(UUID commentId);
}
