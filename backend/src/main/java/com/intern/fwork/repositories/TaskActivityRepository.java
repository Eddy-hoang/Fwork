package com.intern.fwork.repositories;

import com.intern.fwork.entities.TaskActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {
    
    @Query("SELECT ta FROM TaskActivity ta LEFT JOIN FETCH ta.actor WHERE ta.task.id = :taskId ORDER BY ta.createdAt DESC")
    List<TaskActivity> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") UUID taskId);

    @Query(value = "SELECT ta FROM TaskActivity ta LEFT JOIN FETCH ta.actor WHERE ta.task.id = :taskId ORDER BY ta.createdAt DESC",
           countQuery = "SELECT COUNT(ta) FROM TaskActivity ta WHERE ta.task.id = :taskId")
    Page<TaskActivity> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") UUID taskId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM TaskActivity ta WHERE ta.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") UUID taskId);

    @Modifying
    @Query("DELETE FROM TaskActivity ta WHERE ta.task.column.id = :columnId")
    void deleteByColumnId(@Param("columnId") UUID columnId);
}
