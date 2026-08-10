package com.intern.fwork.controllers;

import com.intern.fwork.dtos.request.AssignTaskRequest;
import com.intern.fwork.dtos.request.CreateTaskRequest;
import com.intern.fwork.dtos.request.MoveTaskRequest;
import com.intern.fwork.dtos.request.UpdateTaskRequest;
import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.TaskResponse;
import com.intern.fwork.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/columns/{columnId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> create(
            @PathVariable UUID columnId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        return ApiResponse.success(taskService.create(columnId, request));
    }

    @GetMapping("/api/columns/{columnId}/tasks")
    public ApiResponse<List<TaskResponse>> getTasksByColumn(@PathVariable UUID columnId) {
        return ApiResponse.success(taskService.getTasksByColumn(columnId));
    }

    @GetMapping("/api/boards/{boardId}/tasks")
    public ApiResponse<List<TaskResponse>> getTasksByBoard(@PathVariable UUID boardId) {
        return ApiResponse.success(taskService.getTasksByBoard(boardId));
    }

    @GetMapping("/api/tasks/{id}")
    public ApiResponse<TaskResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(taskService.getById(id));
    }

    @RequestMapping(value = "/api/tasks/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ApiResponse<TaskResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return ApiResponse.success(taskService.update(id, request));
    }

    @DeleteMapping("/api/tasks/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/api/tasks/{id}/move")
    public ApiResponse<TaskResponse> move(
            @PathVariable UUID id,
            @Valid @RequestBody MoveTaskRequest request
    ) {
        return ApiResponse.success(taskService.move(id, request));
    }

    @PatchMapping("/api/tasks/{id}/assignee")
    public ApiResponse<TaskResponse> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTaskRequest request
    ) {
        return ApiResponse.success(taskService.assign(id, request));
    }
}
