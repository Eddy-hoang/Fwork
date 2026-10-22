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
        private Object columnId;
        private Object column_id;

        public UUID getTargetColumnId() {
            Object col = columnId != null ? columnId : column_id;
            if (col == null) return null;
            String str = col.toString().trim();
            if (str.isEmpty() || "null".equalsIgnoreCase(str) || "undefined".equalsIgnoreCase(str)) return null;
            try {
                return UUID.fromString(str);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Data
    public static class BreakdownRequest {
        private Object taskId;
        private Object task_id;

        public UUID getTargetTaskId() {
            Object t = taskId != null ? taskId : task_id;
            if (t == null) return null;
            String str = t.toString().trim();
            if (str.isEmpty() || "null".equalsIgnoreCase(str) || "undefined".equalsIgnoreCase(str)) return null;
            try {
                return UUID.fromString(str);
            } catch (Exception e) {
                return null;
            }
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
