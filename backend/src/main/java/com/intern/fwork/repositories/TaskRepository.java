package com.intern.fwork.repositories;

import com.intern.fwork.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    
    List<Task> findByColumnIdAndIsArchivedFalseOrderByPositionAsc(UUID columnId);

    @Query("SELECT t FROM Task t WHERE t.column.board.id = :boardId AND t.isArchived = false ORDER BY t.column.position ASC, t.position ASC")
    List<Task> findByBoardId(@Param("boardId") UUID boardId);
}