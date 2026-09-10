package com.intern.fwork.controllers;

import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.TaskActivityResponse;
import com.intern.fwork.services.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @GetMapping("/api/boards/{boardId}/activities")
    public ApiResponse<Page<TaskActivityResponse>> getBoardActivities(
            @PathVariable UUID boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.success(taskActivityService.getByBoard(boardId, pageable, type));
    }
}
