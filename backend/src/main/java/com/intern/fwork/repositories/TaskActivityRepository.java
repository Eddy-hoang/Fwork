package com.intern.fwork.repositories;

import com.intern.fwork.entities.TaskActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {

    @EntityGraph(attributePaths = {"actor"})
    Page<TaskActivity> findByBoardIdOrderByCreatedAtDesc(UUID boardId, Pageable pageable);

    @EntityGraph(attributePaths = {"actor"})
    Page<TaskActivity> findByBoardIdAndActionTypeOrderByCreatedAtDesc(UUID boardId, String actionType, Pageable pageable);

    List<TaskActivity> findByBoardId(UUID boardId);

    @Modifying
    @Query("DELETE FROM TaskActivity ta WHERE ta.targetId = :taskId")
    void deleteByTaskId(@Param("taskId") UUID taskId);

    @Modifying
    @Query("DELETE FROM TaskActivity ta WHERE ta.targetId = :targetId")
    void deleteByTargetId(@Param("targetId") UUID targetId);
}
