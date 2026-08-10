package com.intern.fwork.services;

import com.intern.fwork.dtos.request.CreateTaskRequest;
import com.intern.fwork.dtos.request.MoveTaskRequest;
import com.intern.fwork.dtos.request.UpdateTaskRequest;
import com.intern.fwork.dtos.response.TaskResponse;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse create(UUID columnId, CreateTaskRequest request);

    List<TaskResponse> getTasksByColumn(UUID columnId);

    List<TaskResponse> getTasksByBoard(UUID boardId);

    TaskResponse getById(UUID id);

    TaskResponse update(UUID id, UpdateTaskRequest request);

    void delete(UUID id);

    TaskResponse move(UUID id, MoveTaskRequest request);

}
