package com.intern.fwork.services;

import com.intern.fwork.dtos.request.CreateCommentRequest;
import com.intern.fwork.dtos.request.UpdateCommentRequest;
import com.intern.fwork.dtos.response.CommentResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    CommentResponse create(UUID taskId, CreateCommentRequest request);

    Page<CommentResponse> getCommentsByTask(UUID taskId, Pageable pageable);

    CommentResponse update(UUID commentId, UpdateCommentRequest request);

    void delete(UUID commentId);
}
