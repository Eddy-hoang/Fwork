package com.intern.fwork.dtos.response;

import com.intern.fwork.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDashboardResponse {
    private UUID boardId;
    private String boardTitle;
    private long totalTasks;
    private Map<Priority, Long> tasksByPriority;
    private List<ColumnTaskCount> tasksByColumn;
    private long overdueTasks;
    private long unassignedTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnTaskCount {
        private UUID columnId;
        private String columnName;
        private long taskCount;
    }
}
