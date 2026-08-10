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
    public ApiResponse<List<TaskActivityResponse>> getActivity(@PathVariable UUID taskId) {
        return ApiResponse.success(taskActivityService.getByTask(taskId));
    }
}
