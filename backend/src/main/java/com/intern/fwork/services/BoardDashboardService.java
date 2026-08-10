package com.intern.fwork.services;

import com.intern.fwork.dtos.response.BoardDashboardResponse;

import java.util.UUID;

public interface BoardDashboardService {
    BoardDashboardResponse getDashboard(UUID boardId);
}
