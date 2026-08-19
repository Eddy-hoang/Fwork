package com.intern.fwork.controllers;

import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.services.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @GetMapping("/api/tasks/{taskId}/activity")
    public ApiResponse<org.springframework.data.domain.Page<TaskActivityResponse>> getActivity(
            @PathVariable UUID taskId,
            @org.springframework.data.web.PageableDefault(size = 50, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable
    ) {
        return ApiResponse.success(taskActivityService.getByTask(taskId, pageable));
    }

    @GetMapping("/api/boards/{boardId}/activities")
    public ApiResponse<List<TaskActivityResponse>> getBoardActivities(@PathVariable UUID boardId) {
        return ApiResponse.success(taskActivityService.getByBoard(boardId));
    }
}
