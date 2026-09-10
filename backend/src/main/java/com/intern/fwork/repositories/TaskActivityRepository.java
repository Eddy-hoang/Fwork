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
    
    @Query(value = "SELECT ta FROM TaskActivity ta LEFT JOIN FETCH ta.actor WHERE ta.boardId = :boardId ORDER BY ta.createdAt DESC",
           countQuery = "SELECT COUNT(ta) FROM TaskActivity ta WHERE ta.boardId = :boardId")
    Page<TaskActivity> findByBoardIdOrderByCreatedAtDesc(@Param("boardId") UUID boardId, Pageable pageable);

    @Query(value = "SELECT ta FROM TaskActivity ta LEFT JOIN FETCH ta.actor WHERE ta.boardId = :boardId AND ta.actionType = :actionType ORDER BY ta.createdAt DESC",
           countQuery = "SELECT COUNT(ta) FROM TaskActivity ta WHERE ta.boardId = :boardId AND ta.actionType = :actionType")
    Page<TaskActivity> findByBoardIdAndActionTypeOrderByCreatedAtDesc(@Param("boardId") UUID boardId, @Param("actionType") String actionType, Pageable pageable);

    List<TaskActivity> findByBoardId(UUID boardId);

    @Modifying
    @Query("DELETE FROM TaskActivity ta WHERE ta.targetId = :taskId")
    void deleteByTaskId(@Param("taskId") UUID taskId);

    @Modifying
    @Query("DELETE FROM TaskActivity ta WHERE ta.targetId = :targetId")
    void deleteByTargetId(@Param("targetId") UUID targetId);
}
