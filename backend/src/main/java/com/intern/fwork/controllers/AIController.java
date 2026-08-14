package com.intern.fwork.controllers;

import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.services.AIService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards/{boardId}/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @Data
    public static class GenerateTasksRequest {
        private String goal;
        private Integer count;
        private UUID columnId;
        private UUID column_id;

        public UUID getTargetColumnId() {
            return columnId != null ? columnId : column_id;
        }
    }

    @Data
    public static class BreakdownRequest {
        private UUID taskId;
        private UUID task_id;

        public UUID getTargetTaskId() {
            return taskId != null ? taskId : task_id;
        }
    }

    @PostMapping("/generate-tasks")
    public ApiResponse<Map<String, Object>> generateTasks(
            @PathVariable UUID boardId,
            @RequestBody(required = false) GenerateTasksRequest request
    ) {
        String goal = request != null ? request.getGoal() : null;
        Integer count = request != null ? request.getCount() : 6;
        UUID columnId = request != null ? request.getTargetColumnId() : null;
        return ApiResponse.success(aiService.generateTasks(boardId, goal, count, columnId));
    }

    @PostMapping("/breakdown")
    public ApiResponse<List<Map<String, Object>>> breakdown(
            @PathVariable UUID boardId,
            @RequestBody(required = false) BreakdownRequest request
    ) {
        UUID taskId = request != null ? request.getTargetTaskId() : null;
        return ApiResponse.success(aiService.breakdownTask(boardId, taskId));
    }

    @PostMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@PathVariable UUID boardId) {
        return ApiResponse.success(aiService.getSprintSummary(boardId));
    }
}
