package com.intern.fwork.repositories;

import com.intern.fwork.entities.Task;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {
    
    @Override
    @EntityGraph(attributePaths = {"column", "createdBy", "updatedBy", "assignee", "labels"})
    Optional<Task> findById(UUID id);

    @EntityGraph(attributePaths = {"assignee", "labels", "column"})
    List<Task> findByColumnIdAndIsArchivedFalseOrderByPositionAsc(UUID columnId);

    List<Task> findByColumnId(UUID columnId);

    @Query("SELECT t FROM Task t WHERE t.column.board.id = :boardId AND t.isArchived = false ORDER BY t.column.position ASC, t.position ASC")
    @EntityGraph(attributePaths = {"column", "createdBy", "updatedBy", "assignee", "labels"})
    List<Task> findByBoardId(@Param("boardId") UUID boardId);

    @Override
    @EntityGraph(attributePaths = {"assignee", "labels", "column"})
    List<Task> findAll(Specification<Task> spec, Sort sort);

    List<Task> findByLabelsId(UUID labelId);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.column.id = :columnId")
    void deleteByColumnId(@Param("columnId") UUID columnId);

    // Dashboard Aggregate Queries
    long countByColumnBoardIdAndIsArchivedFalse(UUID boardId);

    long countByColumnBoardIdAndIsArchivedFalseAndDueDateIsNotNullAndDueDateBefore(UUID boardId, LocalDateTime now);

    long countByColumnBoardIdAndIsArchivedFalseAndAssigneeIsNull(UUID boardId);

    @Query("SELECT t.priority, COUNT(t) FROM Task t WHERE t.column.board.id = :boardId AND t.isArchived = false GROUP BY t.priority")
    List<Object[]> countByPriorityForBoard(@Param("boardId") UUID boardId);

    @Query("SELECT t.column.id, COUNT(t) FROM Task t WHERE t.column.board.id = :boardId AND t.isArchived = false GROUP BY t.column.id")
    List<Object[]> countByColumnForBoard(@Param("boardId") UUID boardId);
}