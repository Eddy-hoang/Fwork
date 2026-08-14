package com.intern.fwork.services.impl;

import com.intern.fwork.dtos.response.TaskResponse;
import com.intern.fwork.entities.BoardColumn;
import com.intern.fwork.entities.Task;
import com.intern.fwork.entities.User;
import com.intern.fwork.enums.Priority;
import com.intern.fwork.mappers.TaskMapper;
import com.intern.fwork.repositories.BoardColumnRepository;
import com.intern.fwork.repositories.TaskRepository;
import com.intern.fwork.security.SecurityUtils;
import com.intern.fwork.services.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskMapper taskMapper;
    private final SecurityUtils securityUtils;

    @Override
    public Map<String, Object> generateTasks(UUID boardId, String goal, Integer count, UUID columnId) {
        int n = (count == null || count <= 0) ? 6 : Math.min(count, 10);
        String promptGoal = (goal != null && !goal.isBlank()) ? goal.trim() : "Project Deliverables";

        List<Map<String, String>> templates = List.of(
            Map.of("title", "Define requirements and scope for " + promptGoal, "desc", "Draft detailed specs and acceptance criteria.", "priority", "HIGH"),
            Map.of("title", "Design UI/UX wireframes for " + promptGoal, "desc", "Create user flows and interactive prototypes in Figma.", "priority", "MEDIUM"),
            Map.of("title", "Setup database schema and migrations for " + promptGoal, "desc", "Configure JPA entities and Liquibase/Flyway migrations.", "priority", "HIGH"),
            Map.of("title", "Implement core API endpoints for " + promptGoal, "desc", "Develop REST controllers, DTOs, and service layer logic.", "priority", "URGENT"),
            Map.of("title", "Integrate authentication & authorization for " + promptGoal, "desc", "Verify JWT token validation and role-based permissions.", "priority", "HIGH"),
            Map.of("title", "Build frontend components for " + promptGoal, "desc", "Develop React components and integrate with REST APIs.", "priority", "MEDIUM"),
            Map.of("title", "Write unit and integration tests for " + promptGoal, "desc", "Ensure test coverage over 80% with JUnit & Mockito.", "priority", "LOW"),
            Map.of("title", "Perform security review and performance tuning", "desc", "Optimize DB queries and audit endpoint security.", "priority", "MEDIUM"),
            Map.of("title", "Deploy " + promptGoal + " to staging environment", "desc", "Run CI/CD pipeline and verify deployment health.", "priority", "HIGH"),
            Map.of("title", "Conduct user acceptance testing (UAT)", "desc", "Collect feedback from stakeholders and fix bug reports.", "priority", "MEDIUM")
        );

        List<Map<String, Object>> resultTasks = new ArrayList<>();

        if (columnId != null) {
            BoardColumn column = boardColumnRepository.findById(columnId).orElse(null);
            if (column != null) {
                User currentUser = securityUtils.getCurrentUser();
                int currentPos = taskRepository.findByColumnIdAndIsArchivedFalseOrderByPositionAsc(columnId).size();

                for (int i = 0; i < Math.min(n, templates.size()); i++) {
                    Map<String, String> t = templates.get(i);
                    Priority prio;
                    try {
                        prio = Priority.valueOf(t.get("priority"));
                    } catch (Exception e) {
                        prio = Priority.MEDIUM;
                    }

                    Task task = Task.builder()
                            .title(t.get("title"))
                            .description(t.get("desc"))
                            .priority(prio)
                            .position(currentPos++)
                            .column(column)
                            .createdBy(currentUser)
                            .updatedBy(currentUser)
                            .isArchived(false)
                            .dueDate(LocalDateTime.now().plusDays(7))
                            .build();

                    Task saved = taskRepository.save(task);
                    TaskResponse response = taskMapper.toResponse(saved);
                    
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", response.getId());
                    map.put("title", response.getTitle());
                    map.put("description", response.getDescription());
                    map.put("priority", response.getPriority().name().toLowerCase());
                    map.put("columnId", columnId);
                    map.put("column_id", columnId);
                    resultTasks.add(map);
                }

                Map<String, Object> res = new HashMap<>();
                res.put("tasks", resultTasks);
                return res;
            }
        }

        // Just preview suggestions without saving
        for (int i = 0; i < Math.min(n, templates.size()); i++) {
            Map<String, String> t = templates.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("title", t.get("title"));
            map.put("description", t.get("desc"));
            map.put("priority", t.get("priority").toLowerCase());
            resultTasks.add(map);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("tasks", resultTasks);
        return res;
    }

    @Override
    public List<Map<String, Object>> breakdownTask(UUID boardId, UUID taskId) {
        Task task = null;
        if (taskId != null) {
            task = taskRepository.findById(taskId).orElse(null);
        }

        String taskTitle = (task != null && task.getTitle() != null) ? task.getTitle() : "Task";

        List<Map<String, Object>> subtasks = new ArrayList<>();
        
        Map<String, Object> sub1 = new HashMap<>();
        sub1.put("title", "Research & analyze requirements for: " + taskTitle);
        sub1.put("description", "Gather initial specifications, identify edge cases, and define acceptance criteria.");
        sub1.put("priority", "medium");
        subtasks.add(sub1);

        Map<String, Object> sub2 = new HashMap<>();
        sub2.put("title", "Implement core solution for: " + taskTitle);
        sub2.put("description", "Develop the main functionality and logic required.");
        sub2.put("priority", "high");
        subtasks.add(sub2);

        Map<String, Object> sub3 = new HashMap<>();
        sub3.put("title", "Verify & write unit tests for: " + taskTitle);
        sub3.put("description", "Test manually and write unit test coverage.");
        sub3.put("priority", "medium");
        subtasks.add(sub3);

        return subtasks;
    }

    @Override
    public Map<String, Object> getSprintSummary(UUID boardId) {
        List<BoardColumn> columns = boardColumnRepository.findByBoardIdOrderByPositionAsc(boardId);
        List<Task> allTasks = taskRepository.findByBoardId(boardId);

        int total = allTasks.size();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("headline", "Sprint Overview: " + total + " total tasks across " + columns.size() + " columns. Team velocity is on track.");
        
        List<String> completed = new ArrayList<>();
        List<String> inProgress = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        for (Task t : allTasks) {
            String colName = t.getColumn() != null ? t.getColumn().getName().toLowerCase() : "";
            if (colName.contains("done") || colName.contains("complete") || colName.contains("hoàn thành")) {
                completed.add(t.getTitle());
            } else if (colName.contains("doing") || colName.contains("progress") || colName.contains("đang làm")) {
                inProgress.add(t.getTitle() + " (Priority: " + t.getPriority() + ")");
            } else if (t.getPriority() == Priority.URGENT || t.getPriority() == Priority.HIGH) {
                risks.add(t.getTitle() + " - High priority item in " + t.getColumn().getName());
            }
        }

        if (completed.isEmpty()) {
            completed.add("No tasks completed yet in this sprint.");
        }
        if (inProgress.isEmpty()) {
            inProgress.add("No tasks currently marked as in progress.");
        }
        if (risks.isEmpty()) {
            risks.add("No critical blockers or overdue risks identified.");
        }

        recommendations.add("Prioritize high-priority tasks in the backlog.");
        recommendations.add("Review tasks stuck in progress for more than 3 days.");
        recommendations.add("Conduct a daily standup to align on sprint goal.");

        summary.put("completed", completed);
        summary.put("inProgress", inProgress);
        summary.put("risks", risks);
        summary.put("recommendations", recommendations);

        return summary;
    }
}
