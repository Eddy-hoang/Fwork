package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.request.CreateCommentRequest;
import com.intern.fwork.dtos.request.UpdateCommentRequest;
import com.intern.fwork.dtos.response.CommentResponse;
import com.intern.fwork.entities.Comment;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.TaskActivityAction;
import com.intern.fwork.exceptions.ResourceNotFoundException;
import com.intern.fwork.exceptions.TaskNotFoundException;
import com.intern.fwork.mappers.CommentMapper;
import com.intern.fwork.repositories.CommentRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.CommentService;
import com.intern.fwork.services.PermissionService;
import com.intern.fwork.services.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;
    private final SecurityUtils securityUtils;
    private final PermissionService permissionService;
    private final TaskActivityService taskActivityService;

    @Override
    public CommentResponse create(UUID taskId, CreateCommentRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        permissionService.checkCreateComment(taskId, currentUser.getId());

        Task task = taskRepository.findById(taskId)
                .filter(t -> !t.isArchived() && !t.getColumn().getBoard().isArchived()
                        && !t.getColumn().getBoard().getWorkspace().isArchived())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .task(task)
                .createdBy(currentUser)
                .build();

        CommentResponse response = commentMapper.toResponse(commentRepository.save(comment));
        taskActivityService.log(task, currentUser, TaskActivityAction.COMMENT_ADDED,
                "Comment added by " + currentUser.getName());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTask(UUID taskId) {
        User currentUser = securityUtils.getCurrentUser();
        permissionService.checkCreateComment(taskId, currentUser.getId()); // same as "can access task"

        Task task = taskRepository.findById(taskId)
                .filter(t -> !t.isArchived() && !t.getColumn().getBoard().isArchived()
                        && !t.getColumn().getBoard().getWorkspace().isArchived())
                .orElseThrow(() -> new TaskNotFoundException("Task not found"));

        return commentRepository.findByTaskIdOrderByCreatedAtAsc(task.getId()).stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    public CommentResponse update(UUID commentId, UpdateCommentRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        permissionService.checkUpdateComment(commentId, currentUser.getId());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        comment.setContent(request.getContent());

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    @Override
    public void delete(UUID commentId) {
        User currentUser = securityUtils.getCurrentUser();
        permissionService.checkDeleteComment(commentId, currentUser.getId());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Task task = comment.getTask();
        commentRepository.delete(comment);
        taskActivityService.log(task, currentUser, TaskActivityAction.COMMENT_DELETED,
                "Comment deleted by " + currentUser.getName());
    }
}
