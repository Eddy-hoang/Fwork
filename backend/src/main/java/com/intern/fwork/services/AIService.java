package com.intern.fwork.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AIService {
    Map<String, Object> generateTasks(UUID boardId, String goal, Integer count, UUID columnId);
    List<Map<String, Object>> breakdownTask(UUID boardId, UUID taskId);
    Map<String, Object> getSprintSummary(UUID boardId);
}
