package com.intern.fwork.services;

import com.intern.fwork.dtos.request.AssignTaskRequest;
import com.intern.fwork.dtos.request.CreateTaskRequest;
import com.intern.fwork.dtos.request.MoveTaskRequest;
import com.intern.fwork.dtos.request.TaskLabelsRequest;
import com.intern.fwork.dtos.request.UpdateTaskRequest;
import com.intern.fwork.dtos.response.TaskResponse;
import com.intern.fwork.enums.Priority;

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

    TaskResponse assign(UUID id, AssignTaskRequest request);

    TaskResponse updateLabels(UUID id, TaskLabelsRequest request);

    List<TaskResponse> searchTasks(UUID boardId, String q, Priority priority,
                                   UUID assigneeId, UUID labelId,
                                   Boolean overdue, String sort, String dir);
}
