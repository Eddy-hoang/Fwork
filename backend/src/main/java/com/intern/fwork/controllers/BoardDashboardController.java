package com.intern.fwork.controllers;

import com.intern.fwork.dtos.response.ApiResponse;
import com.intern.fwork.dtos.response.BoardDashboardResponse;
import com.intern.fwork.services.BoardDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BoardDashboardController {

    private final BoardDashboardService boardDashboardService;

    @GetMapping("/api/boards/{boardId}/dashboard")
    public ApiResponse<BoardDashboardResponse> getDashboard(@PathVariable UUID boardId) {
        return ApiResponse.success(boardDashboardService.getDashboard(boardId));
    }
}
